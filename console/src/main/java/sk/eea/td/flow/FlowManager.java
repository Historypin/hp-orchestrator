package sk.eea.td.flow;

import java.util.List;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.activities.Activity;
import sk.eea.td.rest.model.Connector;

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
