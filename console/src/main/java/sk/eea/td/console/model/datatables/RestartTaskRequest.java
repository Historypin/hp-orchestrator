package sk.eea.td.console.model.datatables;

public class RestartTaskRequest {

    private Long lastRunId;

    public RestartTaskRequest() {
    }

    public RestartTaskRequest(Long lastRunId) {
        this.lastRunId = lastRunId;
    }

    public Long getLastRunId() {
        return lastRunId;
    }

    public void setLastRunId(Long lastRunId) {
        this.lastRunId = lastRunId;
    }
}
