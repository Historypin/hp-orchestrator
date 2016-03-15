package sk.eea.td.console.model;

import javax.persistence.*;

@Entity
@Table(name = "log")
public class Log {

    @Id
    @SequenceGenerator(name = "seq_log", sequenceName = "seq_log", initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_log")
    private Long id;

    @Column(name = "task_type")
    private String taskType;

    @Column
    private String status;

    @Column
    private String message;

    @ManyToOne
    @JoinColumn(name = "job_run_id")
    private JobRun jobRun;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @Override public String toString() {
        return "Log{" +
                "id=" + id +
                ", taskType='" + taskType + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
