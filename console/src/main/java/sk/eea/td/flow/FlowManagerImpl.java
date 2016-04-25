package sk.eea.td.flow;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sk.eea.td.console.model.*;
import sk.eea.td.console.model.JobRun.JobRunResult;
import sk.eea.td.console.model.JobRun.JobRunStatus;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.rest.model.Connector;

import java.util.*;

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

    private List<Activity> activities = new ArrayList<>();

    private boolean jobRunning = false; // default value

    private List<Connector> sources = new ArrayList<>();

    public FlowManagerImpl(Connector source) {
        this.sources.add(source);
    }

    public FlowManagerImpl(Connector... sources) {
        this.sources.addAll(Arrays.asList(sources));
    }

    public synchronized void trigger() {
        if (!this.jobRunning) {
            // get next job
            Job job = jobRepository.findFirstByLastJobRunIsNullOrderByIdAsc();
            if(job != null && sources.contains(job.getSource())) {
                this.jobRunning = true;
                // create its run
                JobRun jobRun = new JobRun();
                jobRun.setJob(job);
                // copy params into read-only entity
                Set<Param> paramSet = paramRepository.findByJob(job);
                for(Param param : paramSet) {
                    jobRun.addReadOnlyParam(new ReadOnlyParam(param));
                }
                // save & mark as actual job run for this job
                jobRun =  jobRunRepository.save(jobRun);
                job.setLastJobRun(jobRun);
                jobRepository.save(job);

                startFlow(jobRun);
            }
        }
    }

    /* (non-Javadoc)
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
            this.jobRunning = false;
            persistState(context);
        }
    }

    protected void finishFlow(JobRun context) {
        context.setStatus(JobRunStatus.FINISHED);
        context.setResult(JobRunResult.OK);
    }

    protected void failFlow(JobRun context) {
        context.setStatus(JobRunStatus.STOPPED);
        context.setResult(JobRunResult.FAILED);
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

    public List<Connector> getSources() {
        return sources;
    }

    public void setSources(List<Connector> sources) {
        this.sources = sources;
    }

    public void addSource(Connector source) {
        this.sources.add(source);
    }

    private void logActivityStart(Activity activity, JobRun context){
        logActivity(activity, "started", context);
    }

    private void logActivityEnd(Activity activity, JobRun context){
        logActivity(activity, "ended", context);
    }

    private void logActivity(Activity activity, String message, JobRun context){
        logRepository.save(new Log(new Date(), Log.LogLevel.INFO, String.format("%s has %s", activity.getName(), message),context));
    }
}
