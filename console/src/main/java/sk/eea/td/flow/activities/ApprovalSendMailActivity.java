package sk.eea.td.flow.activities;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;

public class ApprovalSendMailActivity implements Activity {

    @Override
    public void execute(JobRun context) throws FlowException {
    }

    @Override
    public String getName() {
        return "ApprovalSendMailActivity";
    }

    @Override
    public boolean isSleepAfter() {
        return true;
    }
}
