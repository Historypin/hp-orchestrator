package sk.eea.td.console.model;

import java.util.Properties;

import javax.persistence.*;
import java.util.Set;

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

    @OneToMany(cascade=CascadeType.ALL, mappedBy="jobRun")
    private Set<ReadOnlyParam> readOnlyParams;

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

	public Properties getProperties() {
		return this.properties;
	}

	public void setProperties(Properties properties) {
		this.properties=properties;
	}

    public Set<ReadOnlyParam> getReadOnlyParams() {
        return readOnlyParams;
    }

    public void setReadOnlyParams(Set<ReadOnlyParam> readOnlyParams) {
        this.readOnlyParams = readOnlyParams;
    }

    @Override public String toString() {
        return "JobRun{" +
                "id=" + id +
                ", job=" + job +
                ", status=" + status +
                ", result=" + result +
                ", readOnlyParams=" + readOnlyParams +
                ", properties=" + properties +
                '}';
    }
}
