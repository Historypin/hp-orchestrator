package sk.eea.td.console.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import sk.eea.td.console.model.Job;

public interface JobRepository extends PagingAndSortingRepository<Job, Long> {
}

