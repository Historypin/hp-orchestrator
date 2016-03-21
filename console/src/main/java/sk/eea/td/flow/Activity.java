package sk.eea.td.flow;

import sk.eea.td.console.model.JobRun;

public interface Activity {

	void execute(JobRun context) throws FlowException;
}
