package sk.eea.td.flow;

import sk.eea.td.console.model.JobRun;

public interface Activity {

	public void execute(JobRun context) throws FlowException;
}
