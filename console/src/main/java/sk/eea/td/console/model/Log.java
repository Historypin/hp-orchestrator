package sk.eea.td.console.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "log")
public class Log {

    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    @Id
    @SequenceGenerator(name = "seq_log", sequenceName = "seq_log", initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_log")
    private Long id;

    @Column
    private Date timestamp;

    @Column
    @Enumerated(EnumType.STRING)
    private LogLevel level;

    @Column
    private String message;

    @ManyToOne
    @JoinColumn(name = "job_run_id")
    private JobRun jobRun;

    public Log() {
    }

    public Log(Date timestamp, LogLevel level, String message, JobRun jobRun) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.jobRun = jobRun;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JobRun getJobRun() {
        return jobRun;
    }

    public void setJobRun(JobRun jobRun) {
        this.jobRun = jobRun;
    }

    @Override
    public String toString() {
        return "Log{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", level=" + level +
                ", message='" + message + '\'' +
                '}';
    }
}
