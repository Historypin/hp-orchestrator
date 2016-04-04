package sk.eea.td.console.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import sk.eea.td.console.model.Log;

import java.util.List;

public interface LogRepository extends CrudRepository<Log, Long> {

    @Query(value = "SELECT * FROM log WHERE job_run_id IN (SELECT max(id) FROM job_run GROUP BY job_id) ORDER BY id DESC LIMIT 200;", nativeQuery = true)
    List<Log> findAllRelevantLogs();
}
