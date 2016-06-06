package sk.eea.td.flow.activities;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.FlowException;
import sk.eea.td.flow.activities.Activity.ActivityAction;

public class TransformAndStoreActivity implements Activity {

    @Override
    public ActivityAction execute(JobRun context) throws FlowException {
        // TODO Auto-generated method stub
    	return ActivityAction.CONTINUE;
    }

    @Override public String getName() {
        return "Tansform and store activity";
    }
}
