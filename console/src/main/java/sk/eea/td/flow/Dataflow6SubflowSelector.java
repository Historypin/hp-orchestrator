package sk.eea.td.flow;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.AbstractJobRun.JobRunResult;
import sk.eea.td.console.model.AbstractJobRun.JobRunStatus;
import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.JobSubRun;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.StringReadOnlyParam;
import sk.eea.td.console.repository.JobRunRepository;

@Component
public class Dataflow6SubflowSelector implements JobSelector {

    @Autowired
    JobRunRepository jobRunRepository;
    
    private TemporalAmount harvestPeriod;

    public Dataflow6SubflowSelector(TemporalAmount harvestPeriod) {
        this.harvestPeriod = harvestPeriod;
    }
    
    /**
     * Prepares AbstractJobRun for dataflow-6, to harvest ArtTag, or to continue after approval of tags.  
     */
    @Override
    public AbstractJobRun nextJobRun(Connector source, Connector target) throws FlowException {
        if(Connector.TAGAPP.equals(source))
        {
            Instant lastRun = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(harvestPeriod);
            
            Page<AbstractJobRun> result = jobRunRepository.findDataflow6SubflowJobRun(Connector.TAGAPP, Date.from(lastRun), new PageRequest(0, 1));
            AbstractJobRun jobRun = result.getContent().isEmpty() ? null : result.getContent().get(0);
            if(jobRun == null)
                return null;

            if(jobRun.getStatus().equals(AbstractJobRun.JobRunStatus.RESUMED) || JobSubRun.class.isAssignableFrom(jobRun.getClass())){
                return jobRun;
            }else{            
                return createNewSubRun4Harvest((JobRun)jobRun);

            }
        }else {
            throw new FlowException(MessageFormat.format("{0} is for source: {1} only. You cannot use it with source {2}", Dataflow6SubflowSelector.class.getSimpleName(), Connector.TAGAPP, source));
        }
        
    }

    private AbstractJobRun createNewSubRun4Harvest(JobRun jobRun) {
        Page<JobSubRun> subRuns = jobRunRepository.findByParentRunAndResultOrderByCreatedDesc(jobRun, JobRunResult.OK, new PageRequest(0, 1));

        JobSubRun subRun = new JobSubRun();
        subRun.setJob(jobRun.getJob());
        subRun.setParentRun(jobRun);
        subRun.setStatus(JobRunStatus.NEW);
        subRun.getReadOnlyParams().addAll(jobRun.getReadOnlyParams().stream().map((param) ->  param.newInstance()).collect(Collectors.toList()));
        
        if(subRuns.hasContent()){
            subRun.addReadOnlyParam(new StringReadOnlyParam(ParamKey.LAST_SUCCESS, DateTimeFormatter.ISO_INSTANT.format(subRuns.getContent().get(0).getLastStarted().toInstant())));
        }
        subRun = jobRunRepository.save(subRun);
        jobRun.setLastJobRun(subRun);
        jobRunRepository.save(jobRun);
        return subRun;
    }

}
