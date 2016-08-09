package sk.eea.td.flow.activities;

import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.flow.FlowException;

public class TransformAndStoreActivity implements Activity {

    @Override
    public ActivityAction execute(AbstractJobRun context) throws FlowException {
        // TODO Auto-generated method stub
    	return ActivityAction.CONTINUE;
    }

    @Override public String getName() {
        return "Tansform and store activity";
    }
}
