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
    private Process process;

    public ReadOnlyParam() {
    }

    public ReadOnlyParam(String key, String value, Process process) {
        this.key = key;
        this.value = value;
        this.process = process;
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

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    @Override public String toString() {
        return "ReadOnlyParam{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
