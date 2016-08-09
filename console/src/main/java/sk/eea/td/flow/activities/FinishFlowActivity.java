/**
 * 
 */
package sk.eea.td.flow.activities;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.AbstractJobRun.JobRunStatus;
import sk.eea.td.console.model.JobSubRun;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.service.CleanupService;
import sk.eea.td.util.ParamUtils;

/**
 * Activity finishes all job runs belonging to parent job run, and resumes parent job run if is waiting.
 * @author Maros Strmensky
 *
 */
public class FinishFlowActivity implements Activity {
    
    @Autowired
    JobRunRepository jobRunRepository;
    
    @Autowired
    CleanupService cleanupService;
    
    @Autowired
    LogRepository logRepository;

    /* (non-Javadoc)
     * @see sk.eea.td.flow.activities.Activity#execute(sk.eea.td.console.model.AbstractJobRun)
     */
    @Override
    public ActivityAction execute(AbstractJobRun context) throws FlowException {
        if(Boolean.valueOf(ParamUtils.copyStringReadOnLyParamsIntoStringParamMap(context.getReadOnlyParams()).get(ParamKey.FINISH_FLOW))){
            List<AbstractJobRun> jobRuns = jobRunRepository.findByJob(context.getJob());
            if(context instanceof JobSubRun){
                AbstractJobRun parentRun = ((JobSubRun)context).getParentRun();
                jobRuns.remove(parentRun);
                if(parentRun.getStatus().equals(JobRunStatus.WAITING)){
                    parentRun.setStatus(JobRunStatus.RESUMED);
                    jobRunRepository.save(parentRun);
                }
            }
            StringBuilder message = new StringBuilder();
            jobRuns.forEach(jobRun -> {
                if(!jobRun.getStatus().equals(JobRunStatus.FINISHED) || !jobRun.getStatus().equals(JobRunStatus.STOPPED)) jobRun.setStatus(JobRunStatus.FINISHED);
                Log log = new Log();
                log.setJobRun(jobRun);
                log.setLevel(Log.LogLevel.INFO);
                log.setMessage(MessageFormat.format("Finished with job run: {0}", context.getId()));
                logRepository.save(log);
                message.append(jobRun.getId()).append(", ");
            });
            jobRunRepository.save(jobRuns);
            cleanupService.cleanUp(jobRuns);
        }
        return ActivityAction.CONTINUE;
    }

    /* (non-Javadoc)
     * @see sk.eea.td.flow.activities.Activity#getName()
     */
    @Override
    public String getName() {
        return FinishFlowActivity.class.getSimpleName();
    }

}
