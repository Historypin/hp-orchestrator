package sk.eea.td.console.repository;

import org.springframework.data.repository.CrudRepository;
import sk.eea.td.console.model.JobRun;

public interface JobRunRepository extends CrudRepository<JobRun, Long> {
}
