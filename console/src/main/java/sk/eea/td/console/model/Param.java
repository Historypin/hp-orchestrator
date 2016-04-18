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
    @Enumerated(EnumType.STRING)
    private ParamKey key;

    @Column
    private String value;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    public Param() {
    }

    public Param(ParamKey key, String value) {
        this.key = key;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ParamKey getKey() {
        return key;
    }

    public void setKey(ParamKey key) {
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
                ", key=" + key +
                ", value='" + value + '\'' +
                ", jobId=" + ((job != null) ? job.getId() : null) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Param param = (Param) o;

        return getId() != null ? getId().equals(param.getId()) : param.getId() == null;

    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
