package sk.eea.td.console.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import sk.eea.td.console.model.Log;

import java.util.List;

public interface LogRepository extends PagingAndSortingRepository<Log, Long> {

    @Query("select l from Log l where l.jobRun.id = ?1")
    List<Log> findByJobRunId(Long jobRunId);

    @Query("select l from Log l where l.jobRun.id = ?1")
    Page<Log> findByJobRunId(Long jobRunId, Pageable pageable);
}
