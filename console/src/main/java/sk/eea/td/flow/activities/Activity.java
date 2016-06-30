package sk.eea.td.flow.activities;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.FlowException;

public interface Activity {
	
	public enum ActivityAction {
		/**
		 * After this activity continue with next activity.
		 */
		CONTINUE, 
		/**
		 * After this activity process should wait for external input.
		 */
		SLEEP, 
		/**
		 * After this activity process should be restarted in next cycle.
		 */
		NEXT_CYCLE;
	};

	ActivityAction execute(JobRun context) throws FlowException;

	String getName();

	default String getId() {
	    return getName();
	};
}
