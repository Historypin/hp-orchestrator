package sk.eea.td.console.model;

import javax.persistence.*;

@Entity
@Table(name = "job")
public class Job {

    @Id
    @SequenceGenerator(name = "seq_job", sequenceName = "seq_job", initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_job")
    private Long id;

    @Column
    private String name;

    @Column
    private String source;

    @Column
    private String target;

    @Column
    private String status;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override public String toString() {
        return "Job{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
