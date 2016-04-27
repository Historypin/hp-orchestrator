package sk.eea.td.console.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import sk.eea.td.console.model.Job;
import sk.eea.td.rest.model.Connector;

import java.util.List;

public interface JobRepository extends PagingAndSortingRepository<Job, Long> {

    Job findFirstByLastJobRunIsNullAndSourceIsInOrderByIdAsc(List<Connector> sources);

    List<Job> findTop20ByOrderByIdDesc();
}
