package sk.eea.td.console.repository;

import org.springframework.data.repository.CrudRepository;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;

import java.util.List;

public interface JobRunRepository extends CrudRepository<JobRun, Long> {

    List<JobRun> findByJob(Job job);

    JobRun findTopByJobOrderByIdDesc(Job job);
}
