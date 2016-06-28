package sk.eea.td.flow;

import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.JobRun;

/**
 * @author Maros Strmensky
 * Interface for selecting jobs.
 */
public interface JobSelector {

	/**
	 * Return first JobRun in queue for specified source and target. 
	 * @param source
	 * @param target
	 * @return
	 * @throws FlowException 
	 */
	JobRun nextJobRun(Connector source, Connector target) throws FlowException;
}
