package sk.eea.td.console.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import sk.eea.td.console.model.Log;

import java.util.List;

public interface LogRepository extends PagingAndSortingRepository<Log, Long> {

    @Deprecated
    @Query(value = "SELECT * FROM log WHERE job_run_id IN (SELECT max(id) FROM job_run GROUP BY job_id)", nativeQuery = true)
    List<Log> findAllRelevantLogs();

    @Query("select l from Log l where l.jobRun.id = ?1") Page<Log>
    findByJobRunId(Long jobRunId, Pageable pageable);

}
