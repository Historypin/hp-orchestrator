package sk.eea.td.console.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
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
    @Enumerated(EnumType.STRING)
    private Connector target;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "job", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Param> params = new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="last_job_run_id", nullable = true)
    private JobRun lastJobRun;

    @Column(insertable = false, updatable = false)
    private Date created;

    @ManyToOne(optional=false, fetch = FetchType.EAGER)
    @JoinColumn(name="\"user\"", nullable=false)
    private User user;

    public Connector getSource() {
        return source;
    }

    public void setSource(Connector source) {
        this.source = source;
    }

    public Connector getTarget() {
        return target;
    }

    public void setTarget(Connector target) {
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

    public JobRun getLastJobRun() {
        return lastJobRun;
    }

    public void setLastJobRun(JobRun lastJobRun) {
        this.lastJobRun = lastJobRun;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void addParam(Param param) {
        param.setJob(this);
        params.add(param);
    }

    public void removeParam(Param param) {
        param.setJob(null);
        params.remove(param);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override public String toString() {
        return "Job{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", source=" + source +
                ", target='" + target + '\'' +
                ", params size=" + ((params != null) ? params.size() : null) +
                ", lastJobRunId=" + ((lastJobRun != null) ? lastJobRun.getId() : null ) +
                ", created=" + created +
                ", username=" + ((user != null) ?  user.getUsername() : null) +
                '}';
    }
}
