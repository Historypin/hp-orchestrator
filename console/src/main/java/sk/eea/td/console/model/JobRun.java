package sk.eea.td.console.model;

import java.util.Properties;

import javax.persistence.*;

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

	private Properties properties;

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

    @Override public String toString() {
        return "JobRun{" +
                "id=" + id +
                ", status=" + status +
                ", result=" + result +
                '}';
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

	public Properties getProperties() {
		return this.properties;
	}

	public void setProperties(Properties properties) {
		this.properties=properties;
	}
}
