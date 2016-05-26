/**
 * 
 */
package sk.eea.td.flow;

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
public class SingleRunJobSelector implements JobSelector {
	
	private static Logger LOG = LoggerFactory.getLogger(SingleRunJobSelector.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRunRepository jobRunRepository;

	/* (non-Javadoc)
	 * @see sk.eea.td.flow.JobSelector#nextJobRun(sk.eea.td.rest.model.Connector, sk.eea.td.rest.model.Connector)
	 */
	@Override
	public JobRun nextJobRun(Connector source, Connector target){
		
        // get next job run
        JobRun jobRun = jobRunRepository.findNextJobRun(source.name(), target.name());
        if (jobRun != null) {
            LOG.debug("jobRun found: {}", jobRun.toString());

            Job job = jobRun.getJob();
            job.setLastJobRun(jobRun);
            jobRepository.save(job);

		    return jobRun;
		}
		return null;
	}
}
