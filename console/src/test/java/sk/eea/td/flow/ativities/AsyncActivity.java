package sk.eea.td.flow.ativities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;

public class AsyncActivity implements Activity {

    private String id;
    private static final Logger LOG = LoggerFactory.getLogger(AsyncActivity.class);

    public AsyncActivity() {
    }
    public AsyncActivity(String id) {
        this.id = id;
    }

    @Override
    public void execute(JobRun context) throws FlowException {
        LOG.info("execute async activity");
    }

    @Override
    public String getName() {
        return "Async";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
