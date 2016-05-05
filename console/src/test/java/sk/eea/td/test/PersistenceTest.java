package sk.eea.td.test;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import sk.eea.td.IntegrationTest;
import sk.eea.td.config.PersistenceConfig;
import sk.eea.td.config.RESTClientsConfig;
import sk.eea.td.config.TestConfig;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.User;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.UsersRepository;
import sk.eea.td.rest.model.Connector;

import java.util.Arrays;
import java.util.List;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class, RESTClientsConfig.class, PersistenceConfig.class})
@Category(IntegrationTest.class)
@Transactional
public class PersistenceTest {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRunRepository jobRunRepository;

    @Test
    public void test() {
        User user = usersRepository.findByUsername("admin");

        for (int i = 0; i < 10; i++) {
            Job job = new Job();
            job.setUser(user);
            jobRepository.save(job);
        }

        for (int i = 0; i < 10; i++) {
            List<Connector> connectors = Arrays.asList(Connector.EUROPEANA);
            Job job = jobRepository.findFirstByLastJobRunIsNullAndSourceIsInOrderByIdAsc(connectors);
            JobRun jobRun = new JobRun();
            jobRun.setJob(job);

            jobRunRepository.save(jobRun);
            job.setLastJobRun(jobRun);
            jobRepository.save(job);

            System.out.println(job.getId());
        }
    }
}
