package sk.eea.td.flow.activities;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.FlowException;

/**
 * Activity which does nothing just puts flow into sleep.
 * @author Maros Strmensky
 *
 */
public class SleepActivity implements Activity {

    @Override
    public ActivityAction execute(JobRun context) throws FlowException {
        return ActivityAction.SLEEP;
    }

    @Override
    public String getName() {
        return SleepActivity.class.getSimpleName();
    }

}
