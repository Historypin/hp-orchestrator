package sk.eea.td.console.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class JobRun extends AbstractJobRun {

    @OneToOne
    @JoinColumn(name="lastrun_id")
    private JobSubRun lastJobRun;

    public void setLastJobRun(JobSubRun lastJobRun) {
        this.lastJobRun = lastJobRun;
    }

    public JobSubRun getLastJobRun() {
        return lastJobRun;
    }
}
