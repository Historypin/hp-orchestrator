package sk.eea.td.console.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "read_only_param")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type",discriminatorType=DiscriminatorType.STRING)
public abstract class ReadOnlyParam<T extends ReadOnlyParam<?>> {

    @Id
    @SequenceGenerator(name = "seq_read_only_param", sequenceName = "seq_read_only_param", initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_read_only_param")
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    private ParamKey key;

    @ManyToOne
    @JoinColumn(name = "job_run_id", nullable = false)
    private AbstractJobRun jobRun;

    public ReadOnlyParam() {
    }

    public ReadOnlyParam(ParamKey key) {
        this.key = key;
    }

    public ReadOnlyParam(ParamKey key, AbstractJobRun jobRun) {
        this.key = key;
        this.jobRun = jobRun;
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

    public AbstractJobRun getJobRun() {
        return jobRun;
    }

    public void setJobRun(AbstractJobRun jobRun) {
        this.jobRun = jobRun;
    }

    public abstract T newInstance();
    
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ReadOnlyParam<?> that = (ReadOnlyParam<?>) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
