/**
 * 
 */
package sk.eea.td.flow;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sk.eea.td.console.model.*;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.ParamRepository;

/**
 * @author Maros Strmensky
 *
 */
@Component
public class Dataflow4JobSelector implements JobSelector {
	
	private static final Connector TARGET = Connector.EUROPEANA_ANNOTATION;

	private static final Connector SOURCE = Connector.HISTORYPIN_ANNOTATION;

	private static Logger LOG = LoggerFactory.getLogger(Dataflow4JobSelector.class);

	@Autowired
	private JobRepository jobRepository;
	
    @Autowired
    private JobRunRepository jobRunRepository;
    
    @Autowired
    private ParamRepository paramRepository;

	/* (non-Javadoc)
	 * @see sk.eea.td.flow.JobSelector#nextJobRun(sk.eea.td.rest.model.Connector, sk.eea.td.rest.model.Connector)
	 */
	@Override
	public JobRun nextJobRun(Connector source, Connector target) throws FlowException {
		if(!SOURCE.equals(source) || !TARGET.equals(target)){
			throw new FlowException(MessageFormat.format("Invalid flow configuration. Selector: {0} should be use only with {1} -> {2} flow.", Dataflow4JobSelector.class.getName(), SOURCE, TARGET));
		}
        // get next job run
		Instant lastRun = Instant.now().truncatedTo(ChronoUnit.DAYS);
		JobRun jobRun = jobRunRepository.findDataflow4JobRun(Date.from(lastRun));
        if (jobRun != null) {
            LOG.debug("jobRun found: {}", jobRun.toString());

            Job job = jobRun.getJob();
            job.setLastJobRun(jobRun);
            jobRepository.save(job);
            ReadOnlyParam lastSuccess = null;
            for(ReadOnlyParam param:jobRun.getReadOnlyParams()){
            	if(param.getKey().equals(ParamKey.LAST_SUCCESS)){
            		lastSuccess = param;
            		break;
            	}
            }

            jobRun.clearReadonlyParams();
            jobRunRepository.save(jobRun);
            
            Set<Param> params = paramRepository.findByJob(job);
            if(lastSuccess != null){
            	jobRun.addReadOnlyParam(new ReadOnlyParam(lastSuccess.getKey(),lastSuccess.getValue()));
            }
            params.stream().forEach(param -> jobRun.addReadOnlyParam(new ReadOnlyParam(param)));
            
		    return jobRunRepository.save(jobRun);
		}
		return null;
	}
}
