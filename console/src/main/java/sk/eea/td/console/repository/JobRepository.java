package sk.eea.td.console.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import sk.eea.td.console.model.Job;
import sk.eea.td.rest.model.Connector;

public interface JobRepository extends PagingAndSortingRepository<Job, Long> {

    @Query(value = "SELECT job.id, job.name, job.source, job.target, job.created, job.last_job_run_id, job.user FROM job LEFT JOIN job_run ON job.id = job_run.job_id WHERE (job_id IS NULL OR (job_id IS NOT NULL AND job_run.status = 'RESUMED')) AND (source = :source) AND (target = :target) ORDER BY job_id LIMIT 1;", nativeQuery = true)
    Job findNextJob(@Param("source") String source, @Param("target") String target);

    Job findFirstByLastJobRunIsNullAndSourceIsInOrderByIdAsc(List<Connector> sources);

    List<Job> findTop20ByOrderByIdDesc();
}
