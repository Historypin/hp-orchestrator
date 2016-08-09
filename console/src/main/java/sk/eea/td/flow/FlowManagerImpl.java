package sk.eea.td.flow;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.AbstractJobRun.JobRunResult;
import sk.eea.td.console.model.AbstractJobRun.JobRunStatus;
import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.StringReadOnlyParam;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.flow.activities.Activity;
import sk.eea.td.flow.activities.Activity.ActivityAction;
import sk.eea.td.rest.service.MailService;
import sk.eea.td.util.DateUtils;

public class FlowManagerImpl implements FlowManager {

    private static final Logger LOG = LoggerFactory.getLogger(FlowManagerImpl.class);


    @Autowired
    private JobRunRepository jobRunRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private MailService mailService;
    
    private JobSelector jobSelector;

    private Lock lock = new ReentrantLock();

    private List<Activity> activities = new ArrayList<>();

    private Connector source;
    private Connector target;

    public FlowManagerImpl(Connector source, Connector target, JobSelector jobSelector) {
        this.source = source;
        this.target = target;
        this.jobSelector = jobSelector;
    }

    /*
     * (non-Javadoc)
     * @see sk.eea.td.flow.FlowManager#startFlow(sk.eea.td.flow.model.FlowConfig)
     */
    public AbstractJobRun startFlow(AbstractJobRun context) {
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

            context = failFlow(context);
        } finally {
            persistState(context);
            LOG.debug("Finished a JobRun with id: {}.", context.getId());
        }
        return context;
    }

    protected AbstractJobRun finishFlow(AbstractJobRun context) {
        context.setStatus(JobRunStatus.FINISHED);
        context.setResult(JobRunResult.OK);

        context = persistState(context);

        return context;
    }

    protected AbstractJobRun failFlow(AbstractJobRun context) {
        context.setStatus(JobRunStatus.STOPPED);
        context.setResult(JobRunResult.FAILED);
        context = persistState(context);

        context = reportFailure(context);

        return context;
    }

    protected AbstractJobRun reportFailure(AbstractJobRun context) {
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

        return context;
    }

    public void trigger() {
        if (lock.tryLock()) {
            try {
				AbstractJobRun jobRun = jobSelector.nextJobRun(source,target);
                if(jobRun != null){
                	LOG.debug("Starting/resuming a JobRun with id: {}.", jobRun.getId());		
                	resumeFlow(jobRun);
                }else{
                	LOG.debug(MessageFormat.format("Nothing to run for Source: {0} -> Destination: {1}.", source, target));
                }
            } catch (FlowException e){
            	LOG.error("Error starting a flow:",e);
            } finally {
                lock.unlock();
            }
        }
    }

    public AbstractJobRun resumeFlow(AbstractJobRun context) {

        try {
        	context.setLastStarted(new Date());
            while (true) {
                Activity activity = getNextActivity(context.getActivity(), context.getStatus());
                if (activity == null) {
                    context = finishFlow(context);
                    break;
                }
                context.setActivity(activity.getId());
                if (JobRunStatus.WAITING != context.getStatus()) {
                    context.setStatus(JobRunStatus.RUNNING);
                    logActivityStart(activity, context);
                    ActivityAction action = activity.execute(context);
                    switch(action){
	                    case SLEEP:
	                        context.setStatus(JobRunStatus.WAITING);
	                        context = persistState(context);
	                        break;
	                    case NEXT_CYCLE:
	                        context.setStatus(JobRunStatus.NEW);
	                        context = persistState(context);
	                        logActivityEnd(activity, context);
	                        break;                    	
	                    default:
	                        context = persistState(context);
	                        logActivityEnd(activity, context);
	                        break;
                    }
                    if(action != ActivityAction.CONTINUE){
                    	break;
                    }
                }
            }
            context.addReadOnlyParam(new StringReadOnlyParam(ParamKey.LAST_SUCCESS, DateUtils.SYSTEM_TIME_FORMAT.format(context.getLastStarted().toInstant())));
        } catch (Throwable e) {
            LOG.error("Exception at executing flow:", e);

            Log log = new Log();
            log.setJobRun(context);
            log.setTimestamp(new Date());
            log.setLevel(Log.LogLevel.ERROR);
            log.setMessage(ExceptionUtils.getStackTrace(e));
            logRepository.save(log);

            context = failFlow(context);
        } finally {
            context = persistState(context);
        }

        return context;
    }

    private Activity getNextActivity(String id, JobRunStatus status) {

        List<Activity> activities = getActivities();
        if (JobRunStatus.NEW.equals(status)) {
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
    public AbstractJobRun persistState(AbstractJobRun config) {
        return jobRunRepository.save(config);
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    protected void logActivityStart(Activity activity, AbstractJobRun context) {
        logActivity(activity, "started", context);
    }

    protected void logActivityEnd(Activity activity, AbstractJobRun context) {
        logActivity(activity, "ended", context);
    }

    protected void logActivity(Activity activity, String message, AbstractJobRun context) {
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
