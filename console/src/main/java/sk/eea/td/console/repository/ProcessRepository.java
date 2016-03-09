package sk.eea.td.console.repository;

import org.springframework.data.repository.CrudRepository;
import sk.eea.td.console.model.Process;

public interface ProcessRepository extends CrudRepository<Process, Long> {
}
