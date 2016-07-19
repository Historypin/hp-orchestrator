package sk.eea.td.flow;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Connector;
import sk.eea.td.flow.activities.Activity;

public interface FlowManager {

	/** 
	 * Initialize and start flow
	 * @param context Flow context
	 * @throws FlowException
	 */
	JobRun startFlow(JobRun context);

	/**
	 * Persist state of flow.
	 * @param config
	 */
	JobRun persistState(JobRun config);

	/**
	 * Run trigger function.
	 *
	 * Used to give time interval to flow manager.
	 */
	void trigger();

    void addActivity(Activity activity);

    void setSource(Connector source);
    Connector getSource();

    void setTarget(Connector target);
}
