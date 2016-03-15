package sk.eea.td.flow;

import sk.eea.td.console.model.Process;
import sk.eea.td.flow.model.Status;

public interface FlowManager {

	/** 
	 * Initialize and start flow
	 * @param context Flow context 
	 * @throws FlowException
	 */
	void startFlow(Process context);

	/**
	 * Persist state of flow.
	 * @param config
	 */
	void persistState(Process config);
}