package sk.eea.td.console.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import sk.eea.td.console.model.Job;

import java.util.List;

public interface JobRepository extends PagingAndSortingRepository<Job, Long> {

    Job findFirstByLastJobRunIsNullOrderByIdAsc();

    List<Job> findTop20ByOrderByIdDesc();
}
