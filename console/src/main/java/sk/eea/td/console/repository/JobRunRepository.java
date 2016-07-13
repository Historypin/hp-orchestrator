package sk.eea.td.console.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;

public interface JobRunRepository extends PagingAndSortingRepository<JobRun, Long> {

    @Query(value = "SELECT job_run.id, job_run.job_id, job_run.status, job_run.result, job_run.activity, job_run.created, job_run.last_started FROM job_run INNER JOIN job ON job.id = job_run.job_id WHERE (job_run.status = 'NEW' OR job_run.status = 'RESUMED') AND (source = :source) AND (target = :target) ORDER BY job_id LIMIT 1", nativeQuery = true)
    JobRun findNextJobRun(@Param("source") String source, @Param("target") String target);
    
    @Query(value = "SELECT job_run.id, job_run.job_id, job_run.status, job_run.result, job_run.activity, job_run.created, job_run.last_started FROM job_run INNER JOIN job ON job.id = job_run.job_id WHERE (job_run.status = 'NEW') AND (source = 'HISTORYPIN_ANNOTATION') AND (target = 'EUROPEANA_ANNOTATION') AND (job_run.last_started < :last_start OR job_run.last_started is NULL) ORDER BY job_run.last_started asc LIMIT 1", nativeQuery = true)
    JobRun findDataflow4JobRun(@Param("last_start")Date last_start);

	List<JobRun> findByJob(Job job);

	@Query(value = "SELECT jobRun FROM JobRun jobRun JOIN jobRun.job where jobRun.status = 'WAITING' AND jobRun.job.target = :target AND jobRun.lastStarted < :lastStarted ORDER BY jobRun.lastStarted ASC")
    Page<JobRun> findDataflow6SubflowJobRun(@Param("target") Connector target, @Param("lastStarted") Date lastStarted, Pageable page);
}
