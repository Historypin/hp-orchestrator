package sk.eea.td.console.repository;

import org.springframework.data.repository.CrudRepository;
import sk.eea.td.console.model.ReadOnlyParam;

public interface ReadOnlyParamRepository extends CrudRepository<ReadOnlyParam, Long> {
}
