package sk.eea.td.console.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "job_run")
public abstract class AbstractJobRun {

    public enum JobRunStatus {
        RUNNING, STOPPED, FINISHED, WAITING, RESUMED, NEW
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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "jobRun", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ReadOnlyParam> readOnlyParams = new ArrayList<>();

    @Column
    private String activity;

    @Column(insertable = false, updatable = false)
    private Date created;
    
    @Column(name = "last_started")
    private Date lastStarted;

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

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
    
    public void clearReadonlyParams(){
    	this.readOnlyParams = new ArrayList<ReadOnlyParam>();
    }

    @Override public String toString() {
        return "JobRun{" +
                "id=" + id +
                ", job=" + job +
                ", status=" + status +
                ", result=" + result +
                ", activity=" + activity +
                ", readOnlyParams size=" + readOnlyParams.size() +
                ", created=" + created +
                '}';
    }

    public Date getLastStarted() {
        return lastStarted;
    }

    public void setLastStarted(Date lastStarted) {
        this.lastStarted = lastStarted;
    }
}
