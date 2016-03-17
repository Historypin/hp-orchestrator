package sk.eea.td.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.JobRun.JobRunResult;
import sk.eea.td.console.model.JobRun.JobRunStatus;
import sk.eea.td.console.model.Param;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.rest.model.Connector;

import java.util.*;
import java.util.stream.Collectors;

public class FlowManagerImpl implements FlowManager {

    private static final Logger LOG = LoggerFactory.getLogger(FlowManagerImpl.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRunRepository jobRunRepository;

    @Autowired
    private ParamRepository paramRepository;

    private List<Activity> activities = new ArrayList<>();

    private boolean jobRunning = false; // default value

    private List<Connector> sources = new ArrayList<>();

    public FlowManagerImpl(Connector source) {
        this.sources.add(source);
    }

    public FlowManagerImpl(Connector... sources) {
        this.sources.addAll(Arrays.asList(sources));
    }

    public void trigger() {
        if (!this.jobRunning) {
            // get next job
            Job job = jobRepository.findNextJob();
            if(job != null && sources.contains(job.getSource())) {
                // create its run
                JobRun jobRun = new JobRun();
                jobRun.setJob(job);

                // copy params into read-only entity
                Set<Param> paramList = paramRepository.findByJob(job);
                Set<ReadOnlyParam> readOnlyParamList = paramList.stream().map(param -> new ReadOnlyParam(param, jobRun)).collect(Collectors.toSet());
                jobRun.setReadOnlyParams(readOnlyParamList);
                jobRunRepository.save(jobRun);

                this.jobRunning = true;
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
                persistState(context);
                activity.execute(context);
                persistState(context);
            }
            finishFlow(context);
            persistState(context);
        } catch (FlowException e) {
            LOG.error("Exception at executing flow:", e);
            failFlow(context);
            persistState(context);
        } finally {
            this.jobRunning = false;
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
    public void persistState(JobRun config) {
        jobRunRepository.save(config);
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
}
