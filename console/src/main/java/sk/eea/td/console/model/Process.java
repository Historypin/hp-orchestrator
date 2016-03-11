package sk.eea.td.console.model;

import javax.persistence.*;

@Entity
@Table(name = "process")
public class Process {

    @Id
    @SequenceGenerator(name = "seq_process", sequenceName = "seq_process", initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_process")
    private Long id;

    @ManyToOne
    private Job job;

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
                '}';
    }
}
