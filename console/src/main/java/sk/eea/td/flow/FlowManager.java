package sk.eea.td.flow;

import sk.eea.td.console.model.JobRun;

public interface FlowManager {

	/** 
	 * Initialize and start flow
	 * @param context Flow context 
	 * @throws FlowException
	 */
	void startFlow(JobRun context);

	/**
	 * Persist state of flow.
	 * @param config
	 */
	void persistState(JobRun config);

	void startHarvest();
}
