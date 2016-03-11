package sk.eea.td.console.repository;

import org.springframework.data.repository.CrudRepository;
import sk.eea.td.console.model.Log;

public interface LogRepository extends CrudRepository<Log, Long> {
}
