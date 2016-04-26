package sk.eea.td.flow.ativities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;

public class SyncActivity implements Activity {

    private String id;
    private static final Logger LOG = LoggerFactory.getLogger(SyncActivity.class);

    public SyncActivity() {
    }
    public SyncActivity(String id) {
        this.id = id;
    }

    @Override
    public void execute(JobRun context) throws FlowException {
        LOG.info("execute sync activity");
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
