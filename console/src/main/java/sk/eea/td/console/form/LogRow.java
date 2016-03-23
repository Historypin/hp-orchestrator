package sk.eea.td.console.form;

import java.util.Date;

public class LogRow {

    private Date timestamp;

    private Long taskId;

    private String level;

    private String message;

    public LogRow() {
    }

    public LogRow(Date timestamp, Long taskId, String level, String message) {
        this.timestamp = timestamp;
        this.taskId = taskId;
        this.level = level;
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
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
                "timestamp=" + timestamp +
                ", taskId=" + taskId +
                ", level='" + level + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
