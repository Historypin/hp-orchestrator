package sk.eea.td.console.repository;

import org.springframework.data.repository.CrudRepository;
import sk.eea.td.console.model.Job;

public interface JobRepository extends CrudRepository<Job, Long> {
}
