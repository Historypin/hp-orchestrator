package sk.eea.td.console.model.datatables;

public class RemoveTaskRequest {

    private Long jobId;

    public RemoveTaskRequest() {
    }

    public RemoveTaskRequest(Long jobId) {
        this.jobId = jobId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    @Override
    public String toString() {
        return "RemoveTaskRequest{" +
                "jobId=" + jobId +
                '}';
    }
}
