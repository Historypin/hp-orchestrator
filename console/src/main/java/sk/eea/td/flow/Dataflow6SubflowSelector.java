package sk.eea.td.flow;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.repository.JobRunRepository;

@Component
public class Dataflow6SubflowSelector implements JobSelector {

    @Autowired
    JobRunRepository jobRunRepository;
    
    private TemporalAmount harvestPeriod;

    public Dataflow6SubflowSelector(TemporalAmount harvestPeriod) {
        this.harvestPeriod = harvestPeriod;
    }
    
    @Override
    public JobRun nextJobRun(Connector source, Connector target) throws FlowException {
        if(Connector.TAGAPP.equals(source))
        {
            Instant lastRun = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(harvestPeriod);
            
            Page<JobRun> result = jobRunRepository.findDataflow6SubflowJobRun(Connector.TAGAPP, Date.from(lastRun), new PageRequest(0, 1));
            return result.getContent().isEmpty() ? null : result.getContent().get(0);
        }else {
            throw new FlowException(MessageFormat.format("{0} is for source: {1} only. You cannot use it with source {2}", Dataflow6SubflowSelector.class.getSimpleName(), Connector.TAGAPP, source));
        }
        
    }

}
