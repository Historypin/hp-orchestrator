package sk.eea.td.console.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import sk.eea.td.console.model.Job;

public interface JobRepository extends PagingAndSortingRepository<Job, Long> {

    Page<Job> findByNameIsContainingIgnoreCase(String value, Pageable pageable);
}

