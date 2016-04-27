package sk.eea.td.console.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import sk.eea.td.console.model.Job;

public interface JobRepository extends PagingAndSortingRepository<Job, Long> {

    @Query(value = "SELECT job.id, job.name, job.source, job.target, job.created, job.last_job_run_id, job.user FROM job LEFT JOIN job_run ON job.id = job_run.job_id WHERE job_id IS NULL OR (job_id IS NOT NULL AND job_run.status = 'RESUMED') ORDER BY job_id LIMIT 1;", nativeQuery = true)
    Job findNextJob();

    Job findFirstByLastJobRunIsNullOrderByIdAsc();

    List<Job> findTop20ByOrderByIdDesc();
}
