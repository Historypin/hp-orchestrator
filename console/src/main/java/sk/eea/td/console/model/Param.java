package sk.eea.td.console.model;

import javax.persistence.*;

@Entity
@Table(name = "param")
public class Param {

    @Id
    @SequenceGenerator(name = "seq_param", sequenceName = "seq_param", initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_param")
    private Long id;

    @Column
    private String key;

    @Column
    private String value;

    @ManyToOne
    private Job job;

    public Param() {
    }

    public Param(String key, String value, Job job) {
        this.key = key;
        this.value = value;
        this.job = job;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    @Override public String toString() {
        return "Param{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
