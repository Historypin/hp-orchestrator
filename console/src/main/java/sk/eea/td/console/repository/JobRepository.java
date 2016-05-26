package sk.eea.td.console.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import sk.eea.td.console.model.Job;

public interface JobRepository extends PagingAndSortingRepository<Job, Long> {
    @Query(value = "SELECT job.id, job.name, job.source, job.target, job.created, job.last_job_run_id, job.user FROM job LEFT JOIN job_run ON job.id = job_run.job_id WHERE (job_run.status = 'RESUMED' or job_run.status = NULL) and (source = :source) AND (target = :target) ORDER BY job_run.id asc NULLS FIRST, job_run.created asc LIMIT 1;")
    Job findNextJobLastTimeOrder(@Param("source") String source, @Param("target") String target);    
}

