package sk.eea.td.console.repository;

import org.springframework.data.repository.CrudRepository;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.Param;

import java.util.Set;

public interface ParamRepository extends CrudRepository<Param, Long>{

    Set<Param> findByJob(Job job);
}
