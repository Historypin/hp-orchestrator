package sk.eea.td.console.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_run")
public class JobRun {

    public enum JobRunStatus {
        RUNNING, STOPPED, FINISHED
    }

    public enum JobRunResult {
        OK, FAILED
    }

    @Id
    @SequenceGenerator(name = "seq_job_run", sequenceName = "seq_job_run", initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_job_run")
    private Long id;

    @ManyToOne
    private Job job;

    @Column
    @Enumerated(EnumType.STRING)
    private JobRunStatus status;

    @Column
    @Enumerated(EnumType.STRING)
    private JobRunResult result;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "jobRun", orphanRemoval = true)
    private List<ReadOnlyParam> readOnlyParams = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public JobRunStatus getStatus() {
        return status;
    }

    public void setStatus(JobRunStatus status) {
        this.status = status;
    }

    public JobRunResult getResult() {
        return result;
    }

    public void setResult(JobRunResult result) {
        this.result = result;
    }

    public List<ReadOnlyParam> getReadOnlyParams() {
        return readOnlyParams;
    }

    public void addReadOnlyParam(ReadOnlyParam readOnlyParam) {
        readOnlyParam.setJobRun(this);
        this.readOnlyParams.add(readOnlyParam);
    }

    public void removeReadOnlyParam(ReadOnlyParam readOnlyParam) {
        readOnlyParam.setJobRun(null);
        this.readOnlyParams.remove(readOnlyParam);
    }

    @Override public String toString() {
        return "JobRun{" +
                "id=" + id +
                ", job=" + job +
                ", status=" + status +
                ", result=" + result +
                ", readOnlyParams size=" + readOnlyParams.size() +
                '}';
    }
}
