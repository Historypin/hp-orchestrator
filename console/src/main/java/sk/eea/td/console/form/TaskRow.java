package sk.eea.td.console.form;

public class TaskRow {

    private Long id;

    private String name;

    private String source;

    private String target;

    private String lastRunStatus;

    private String lastRunResult;

    private String lastRunId;

    public TaskRow() {
    }

    public TaskRow(Long id,
            String name,
            String source,
            String target,
            String lastRunStatus,
            String lastRunResult, String lastRunId) {
        this.id = id;
        this.name = name;
        this.source = source;
        this.target = target;
        this.lastRunStatus = lastRunStatus;
        this.lastRunResult = lastRunResult;
        this.lastRunId = lastRunId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getLastRunStatus() {
        return lastRunStatus;
    }

    public void setLastRunStatus(String lastRunStatus) {
        this.lastRunStatus = lastRunStatus;
    }

    public String getLastRunResult() {
        return lastRunResult;
    }

    public void setLastRunResult(String lastRunResult) {
        this.lastRunResult = lastRunResult;
    }

    public String getLastRunId() {
        return lastRunId;
    }

    public void setLastRunId(String lastRunId) {
        this.lastRunId = lastRunId;
    }

    @Override
    public String toString() {
        return "TaskRow{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", lastRunStatus='" + lastRunStatus + '\'' +
                ", lastRunResult='" + lastRunResult + '\'' +
                ", lastRunId='" + lastRunId + '\'' +
                '}';
    }
}
