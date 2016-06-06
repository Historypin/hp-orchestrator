package sk.eea.td.console.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import sk.eea.td.console.model.JobRun;

public interface JobRunRepository extends CrudRepository<JobRun, Long> {

    @Query(value = "SELECT job_run.id, job_run.job_id, job_run.status, job_run.result, job_run.activity, job_run.created, job_run.last_started FROM job_run INNER JOIN job ON job.id = job_run.job_id WHERE (job_run.status = 'NEW' OR job_run.status = 'RESUMED') AND (source = :source) AND (target = :target) ORDER BY job_id LIMIT 1", nativeQuery = true)
    JobRun findNextJobRun(@Param("source") String source, @Param("target") String target);
    
    @Query(value = "SELECT job_run.id, job_run.job_id, job_run.status, job_run.result, job_run.activity, job_run.created, job_run.last_started FROM job_run INNER JOIN job ON job.id = job_run.job_id WHERE (job_run.status = 'NEW') AND (source = 'HISTORYPIN_ANNOTATION') AND (target = 'EUROPEANA_ANNOTATION') AND (job_run.last_started < :last_start OR job_run.last_started is NULL) ORDER BY job_run.last_started asc LIMIT 1", nativeQuery = true)
    JobRun findDataflow4JobRun(@Param("last_start")Date last_start);
}
