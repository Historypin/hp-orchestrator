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
    private String key;

    @Column
    private String value;

    @ManyToOne
    @JoinColumn(name = "job_run_id")
    private JobRun jobRun;

    public ReadOnlyParam() {
    }

    public ReadOnlyParam(String key, String value, JobRun jobRun) {
        this.key = key;
        this.value = value;
        this.jobRun = jobRun;
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

    public JobRun getJobRun() {
        return jobRun;
    }

    public void setJobRun(JobRun jobRun) {
        this.jobRun = jobRun;
    }

    @Override public String toString() {
        return "ReadOnlyParam{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
