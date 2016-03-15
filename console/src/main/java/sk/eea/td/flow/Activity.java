package sk.eea.td.flow;

import sk.eea.td.console.model.Process;

public interface Activity {

	public void execute(Process context) throws FlowException;
}
