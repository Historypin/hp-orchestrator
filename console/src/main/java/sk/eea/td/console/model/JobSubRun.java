package sk.eea.td.console.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class JobSubRun extends JobRun {

    @ManyToOne
    @JoinColumn(name = "parentrun_id")
    private JobRun parentRun;

    public JobRun getParentRun() {
        return parentRun;
    }

    public void setParentRun(JobRun parentRun) {
        this.parentRun = parentRun;
    }  
}
