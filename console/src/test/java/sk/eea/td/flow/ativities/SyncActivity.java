package sk.eea.td.flow.ativities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.flow.FlowException;
import sk.eea.td.flow.activities.Activity;

public class SyncActivity implements Activity {

    private String id;
    boolean sleepAfter = false;
    private static final Logger LOG = LoggerFactory.getLogger(SyncActivity.class);

    public SyncActivity() {
    }
    public SyncActivity(String id, boolean sleepAfter) {
        this.id = id;
        this.sleepAfter = sleepAfter;
    }

    @Override
    public ActivityAction execute(AbstractJobRun context) throws FlowException {
        LOG.info("execute sync activity");
        return sleepAfter? ActivityAction.SLEEP:ActivityAction.CONTINUE;
    }

    @Override
    public String getName() {
        return "Sync";
    }

    @Override
    public String getId() {
        return id;
    }
}
