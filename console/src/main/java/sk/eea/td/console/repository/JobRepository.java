package sk.eea.td.console.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import sk.eea.td.console.model.Job;

import java.util.List;

public interface JobRepository extends CrudRepository<Job, Long> {

    @Query(value = "select job.id, job.name, job.source, job.target from job LEFT JOIN job_run on job.id = job_run.job_id WHERE job_id IS NULL ORDER BY job_id LIMIT 1;", nativeQuery = true)
    Job findNextJob();

    List<Job> findByOrderByIdDesc();
}
