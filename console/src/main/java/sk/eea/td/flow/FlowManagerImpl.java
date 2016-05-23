package sk.eea.td.flow;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sk.eea.td.console.model.*;
import sk.eea.td.console.model.JobRun.JobRunResult;
import sk.eea.td.console.model.JobRun.JobRunStatus;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.rest.model.Connector;
import sk.eea.td.rest.service.MailService;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//@Component
public class FlowManagerImpl implements FlowManager {

    private static final Logger LOG = LoggerFactory.getLogger(FlowManagerImpl.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRunRepository jobRunRepository;

    @Autowired
    private ParamRepository paramRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private MailService mailService;

    private Lock lock = new ReentrantLock();

    private List<Activity> activities = new ArrayList<>();

    private Connector source;
    private Connector target;

    public FlowManagerImpl() {
    }

    public FlowManagerImpl(Connector source, Connector target) {
        this.source = source;
        this.target = target;
    }

    /*
     * (non-Javadoc)
     * @see sk.eea.td.flow.FlowManager#startFlow(sk.eea.td.flow.model.FlowConfig)
     */
    public void startFlow(JobRun context) {
        context.setStatus(JobRunStatus.RUNNING);
        List<Activity> activities = getActivities();
        try {
            for (Activity activity : activities) {
                context = persistState(context);
                logActivityStart(activity, context);

                activity.execute(context);

                context = persistState(context);
                logActivityEnd(activity, context);
            }
            finishFlow(context);
        } catch (Exception e) {
            LOG.error("Exception at executing flow:", e);

            Log log = new Log();
            log.setJobRun(context);
            log.setLevel(Log.LogLevel.ERROR);
            log.setMessage(ExceptionUtils.getStackTrace(e));
            logRepository.save(log);

            failFlow(context);
        } finally {
            persistState(context);
            LOG.debug("Finished a JobRun with id: {}.", context.getId());
        }
    }

    protected void finishFlow(JobRun context) {
        context.setStatus(JobRunStatus.FINISHED);
        context.setResult(JobRunResult.OK);
    }

    protected void failFlow(JobRun context) {
        context.setStatus(JobRunStatus.STOPPED);
        context.setResult(JobRunResult.FAILED);

        reportFailure(context);
    }

    protected void reportFailure(JobRun context) {
        try {
            final Map<String, String> emailParams = new HashMap<>();
            // prepare required params for sending emails
            emailParams.put("userName", context.getJob().getUser().getUsername());
            emailParams.put("taskName", context.getJob().getName());
            emailParams.put("taskRunId", context.getId().toString());

            mailService.sendErrorMail(
                    context.getJob().getUser().getEmail(),
                    "Orchestrator task has failed",
                    emailParams
            );
        } catch (Exception e) {
            LOG.error("Exception occurred during reporting failure to user: ", e);
        }
    }

    public void trigger() {

        if (lock.tryLock()) {
            try {
                // get next job
                Job job = jobRepository.findNextJob(source.name(), target.name());
                LOG.debug("job found: ", job);
                if (job != null /*&& sources.contains(job.getSource())*/) {

                    JobRun jobRun = null;
                    if (job.getLastJobRun() != null && job.getLastJobRun().getStatus() != null
                            && (JobRunStatus.RESUMED == job.getLastJobRun().getStatus())) {
                        jobRun = job.getLastJobRun();
                    } else {
                        jobRun = new JobRun();
                        jobRun.setJob(job);
                        Set<Param> paramList = paramRepository.findByJob(job);
                        for (Param param : paramList) {
                            jobRun.addReadOnlyParam(new ReadOnlyParam(param));
                        }
                    }

                    jobRun = jobRunRepository.save(jobRun);
                    job.setLastJobRun(jobRun);
                    jobRepository.save(job);

                    LOG.debug("Starting/resuming a JobRun with id: {}.", jobRun.getId());
                    resumeFlow(jobRun);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public void resumeFlow(JobRun context) {

        try {
            while (true) {
                Activity activity = getNextActivity(context.getActivity(), context.getStatus());
                if (activity == null) {
                    finishFlow(context);
                    break;
                }
                context.setActivity(activity.getId());
                if (JobRunStatus.WAITING != context.getStatus()) {
                    context.setStatus(JobRunStatus.RUNNING);
                    logActivityStart(activity, context);
                    activity.execute(context);
                    if (activity.isSleepAfter()) {
                        context.setStatus(JobRunStatus.WAITING);
                        context = persistState(context);
                        break;
                    } else {
                        context = persistState(context);
                        logActivityEnd(activity, context);
                    }
                }
            }

        } catch (Exception e) {
            LOG.error("Exception at executing flow:", e);

            Log log = new Log();
            log.setJobRun(context);
            log.setTimestamp(new Date());
            log.setLevel(Log.LogLevel.ERROR);
            log.setMessage(ExceptionUtils.getStackTrace(e));
            logRepository.save(log);

            failFlow(context);
        } finally {
            persistState(context);
        }
    }

    private Activity getNextActivity(String id, JobRunStatus status) {

        List<Activity> activities = getActivities();
        if (status == null) {
            return activities.get(0);
        }

        for (int i = 0; i < activities.size(); i++) {
            Activity activity = activities.get(i);
            if (id.equalsIgnoreCase(activity.getId())) {
                if (JobRunStatus.WAITING == status/* || JobRunStatus.RESUMED == status*/) {
                    return activity;
                } else {
                    return (activities.size() > i + 1) ? activities.get(i + 1) : null;
                }
            }
        }
        return null;
    }

    @Override
    public JobRun persistState(JobRun config) {
        return jobRunRepository.save(config);
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    private void logActivityStart(Activity activity, JobRun context) {
        logActivity(activity, "started", context);
    }

    private void logActivityEnd(Activity activity, JobRun context) {
        logActivity(activity, "ended", context);
    }

    private void logActivity(Activity activity, String message, JobRun context) {
        logRepository.save(new Log(new Date(), Log.LogLevel.INFO, String.format("%s has %s", activity.getName(), message), context));
    }

    @Override
    public void setSource(Connector source) {
        this.source = source;
    }

    @Override
    public Connector getSource() {
        return source;
    }

    @Override
    public void setTarget(Connector target) {
        this.target = target;
    }

}
