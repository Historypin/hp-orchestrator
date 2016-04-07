package sk.eea.td.console.form;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LogRow {

    private String timestamp;

    private Long jobRunId;

    private String level;

    private String message;

    public LogRow() {
    }

    public LogRow(String timestamp, Long taskId, String level, String message) {
        this.timestamp = timestamp;
        this.jobRunId = taskId;
        this.level = level;
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Long getJobRunId() {
        return jobRunId;
    }

    public void setJobRunId(Long jobRunId) {
        this.jobRunId = jobRunId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override public String toString() {
        return "LogRow{" +
                "timestamp='" + timestamp + '\'' +
                ", jobRunId=" + jobRunId +
                ", level='" + level + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
