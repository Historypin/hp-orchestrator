package sk.eea.td.console.model;

import javax.persistence.*;

@Entity
@Table(name = "read_only_param")
public class ReadOnlyParam {

    @Id
    @SequenceGenerator(name = "seq_read_only_param", sequenceName = "seq_read_only_param", initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_read_only_param")
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    private ParamKey key;

    @Column
    private String value;

    @ManyToOne
    @JoinColumn(name = "job_run_id", nullable = false)
    private AbstractJobRun jobRun;

    public ReadOnlyParam() {
    }

    public ReadOnlyParam(ParamKey key, String value) {
        this.key = key;
        this.value = value;
    }

    public ReadOnlyParam(Param param) {
        this.key = param.getKey();
        this.value = param.getValue();
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

    public AbstractJobRun getJobRun() {
        return jobRun;
    }

    public void setJobRun(AbstractJobRun jobRun) {
        this.jobRun = jobRun;
    }

    @Override public String toString() {
        return "ReadOnlyParam{" +
                "id=" + id +
                ", key=" + key +
                ", value='" + value + '\'' +
                ", jobRunId=" + ((jobRun != null) ? jobRun.getId() : null) +
                '}';
    }
}
