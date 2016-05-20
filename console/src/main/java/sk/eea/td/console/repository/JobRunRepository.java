package sk.eea.td.console.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;

import java.util.List;

public interface JobRunRepository extends CrudRepository<JobRun, Long> {

    @Query(value = "SELECT job_run.id, job_run.job_id, job_run.status, job_run.result, job_run.activity, job_run.created FROM job_run INNER JOIN job ON job.id = job_run.job_id WHERE (job_run.status = 'NEW' OR job_run.status = 'RESUMED') AND (source = :source) AND (target = :target) ORDER BY job_id LIMIT 1", nativeQuery = true)
    JobRun findNextJobRun(@Param("source") String source, @Param("target") String target);
}
