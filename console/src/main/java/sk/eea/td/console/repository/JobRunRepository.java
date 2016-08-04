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
import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.AbstractJobRun.JobRunResult;
import sk.eea.td.console.model.JobSubRun;

public interface JobRunRepository extends PagingAndSortingRepository<AbstractJobRun, Long> {

    @Query(value = "SELECT job_run.id, job_run.job_id, job_run.status, job_run.result, job_run.activity, job_run.created, job_run.last_started, job_run.dtype FROM job_run INNER JOIN job ON job.id = job_run.job_id WHERE (job_run.status = 'NEW' OR job_run.status = 'RESUMED') AND (source = :source) AND (target = :target) AND job_run.dtype='JobRun' ORDER BY job_id LIMIT 1", nativeQuery = true)
    JobRun findNextJobRun(@Param("source") String source, @Param("target") String target);
    
    @Query(value = "SELECT job_run.id, job_run.job_id, job_run.status, job_run.result, job_run.activity, job_run.created, job_run.last_started, job_run.dtype FROM job_run INNER JOIN job ON job.id = job_run.job_id WHERE (job_run.status = 'NEW') AND job_run.dtype='JobRun' AND (source = 'HISTORYPIN_ANNOTATION') AND (target = 'EUROPEANA_ANNOTATION') AND (job_run.last_started < :last_start OR job_run.last_started is NULL) ORDER BY job_run.last_started asc LIMIT 1", nativeQuery = true)
    JobRun findDataflow4JobRun(@Param("last_start")Date last_start);

	List<AbstractJobRun> findByJob(Job job);

	@Query(value = "SELECT jobRun FROM AbstractJobRun jobRun JOIN jobRun.job left join jobRun.lastJobRun where jobRun.job.target = :target AND (jobRun.status = 'RESUMED' or (jobRun.dtype = 'JobRun' and jobRun.status = 'WAITING' AND jobRun.lastStarted < :lastStarted AND (jobRun.lastJobRun.lastStarted < :lastStarted or jobRun.lastJobRun.lastStarted is NULL))) ORDER BY jobRun.lastJobRun.lastStarted ASC NULLS FIRST, jobRun.lastStarted ASC")
    Page<AbstractJobRun> findDataflow6SubflowJobRun(@Param("target") Connector target, @Param("lastStarted") Date lastStarted, Pageable page);

    Page<JobSubRun> findByParentRunAndResultOrderByCreatedDesc(AbstractJobRun jobRun, JobRunResult ok, Pageable pageRequest);
}
