/**
 * 
 */
package sk.eea.td.flow;

import java.text.MessageFormat;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.JobRun.JobRunStatus;
import sk.eea.td.console.model.Param;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.rest.model.Connector;

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
    private ParamRepository paramRepository;

    @Autowired
    private JobRunRepository jobRunRepository;

	/* (non-Javadoc)
	 * @see sk.eea.td.flow.JobSelector#nextJobRun(sk.eea.td.rest.model.Connector, sk.eea.td.rest.model.Connector)
	 */
	@Override
	public JobRun nextJobRun(Connector source, Connector target) throws FlowException {
		if(!SOURCE.equals(source) || !TARGET.equals(target)){
			throw new FlowException(MessageFormat.format("Invalid flow configuration. Selector: {0} should be use only with {1} -> {2} flow.", Dataflow4JobSelector.class.getName(), SOURCE, TARGET));
		}
		// get next job
		Job job = jobRepository.findNextJobLastTimeOrder(SOURCE.name(), TARGET.name());
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
		    LOG.debug("Created a new JobRun with id: {}.", jobRun.getId());
		    return jobRun;
		}
		return null;
	}
}
