package sk.eea.td.console.model;

import javax.persistence.*;

@Entity
@Table(name = "process")
public class Process {
	
	public enum ProcessStatus {
		RUNNING, STOPPED, FINISHED;
	}

	public enum ProcessResult {
		OK, FAILED;
	}
	
    @Id
    @SequenceGenerator(name = "seq_process", sequenceName = "seq_process", initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_process")
    private Long id;

    @ManyToOne
    private Job job;
    
    @Column
    private ProcessStatus status;
    
    @Column ProcessResult result;

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
        return "Process{" +
                "id=" + id +
                ", status='" + status + '\'' +                
                '}';
    }

	public ProcessStatus getStatus() {
		return status;
	}

	public void setStatus(ProcessStatus status) {
		this.status = status;
	}

	public ProcessResult getResult() {
		return result;
	}

	public void setResult(ProcessResult result) {
		this.result = result;
	}
}
