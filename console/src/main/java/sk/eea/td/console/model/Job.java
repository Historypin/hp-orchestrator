package sk.eea.td.console.model;

import sk.eea.td.rest.model.Connector;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
    @Enumerated(EnumType.STRING)
    private Connector source;

    @Column
    private String target;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "job", orphanRemoval = true)
    private List<Param> params = new ArrayList<>();

    public Connector getSource() {
        return source;
    }

    public void setSource(Connector source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
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

    public List<Param> getParams() {
        return params;
    }

    public void addParam(Param param) {
        param.setJob(this);
        params.add(param);
    }

    public void removeParam(Param param) {
        param.setJob(null);
        params.remove(param);
    }

    @Override public String toString() {
        return "Job{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", source='" + source + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
