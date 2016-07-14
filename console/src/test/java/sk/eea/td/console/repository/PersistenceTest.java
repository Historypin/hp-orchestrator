package sk.eea.td.console.repository;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import sk.eea.td.IntegrationTest;
import sk.eea.td.config.PersistenceConfig;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.User;
import sk.eea.td.console.model.Connector;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static sk.eea.td.console.model.AbstractJobRun.JobRunStatus.NEW;
import static sk.eea.td.console.model.AbstractJobRun.JobRunStatus.RUNNING;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { PersistenceTestConfig.class, PersistenceConfig.class})
@Category(IntegrationTest.class)
@Transactional
public class PersistenceTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRunRepository jobRunRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private ParamRepository paramRepository;

    @Autowired
    private ReadOnlyParamRepository readOnlyParamRepository;

    @Autowired
    private UsersRepository usersRepository;

    private static final String JOB_NAME = "this is my job name";

    @Test
    public void jobRepositoryTest() {
        // create
        Job job = new Job();
        job = jobRepository.save(job);
        assertThat(job, is(notNullValue(Job.class)));
        assertThat(job.getId(), is(notNullValue(Long.class)));

        // read
        Job sameJob = jobRepository.findOne(job.getId());
        assertThat(sameJob, is(notNullValue(Job.class)));
        assertThat(job.getId(), is(equalTo(sameJob.getId())));

        // update
        job.setName(JOB_NAME);
        jobRepository.save(job);
        sameJob = jobRepository.findOne(job.getId());
        assertThat(sameJob, is(notNullValue(Job.class)));
        assertThat(sameJob.getName(), is(equalTo(JOB_NAME)));

        // delete
        jobRepository.delete(job);
        sameJob = jobRepository.findOne(job.getId());
        assertThat(sameJob, is(nullValue()));
    }

    @Test
    public void jobRunRepositoryTest() {
        // create
        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setEmail("email");
        testUser.setEnabled(false);
        testUser.setPassword("nbusr123");
        testUser = usersRepository.save(testUser);

        Job job = new Job();
        job.setUser(testUser);
        job.setSource(Connector.EUROPEANA);
        job.setTarget(Connector.HISTORYPIN);
        jobRepository.save(job);

        JobRun jobRun = new JobRun();
        jobRun.setStatus(NEW);
        jobRun.setJob(job);
        jobRun = jobRunRepository.save(jobRun);
        assertThat(jobRun, is(notNullValue(JobRun.class)));
        assertThat(jobRun.getId(), is(notNullValue(Long.class)));

        // read
        JobRun nextJobRun = jobRunRepository.findNextJobRun(Connector.EUROPEANA.toString(), Connector.HISTORYPIN.toString());
        assertThat(nextJobRun, is(notNullValue(JobRun.class)));
        assertThat(jobRun.getId(), is(equalTo(jobRun.getId())));

        // update
        jobRun.setStatus(RUNNING);
        jobRunRepository.save(jobRun);
        JobRun sameJobRun = jobRunRepository.findOne(jobRun.getId());
        assertThat(sameJobRun, is(notNullValue(JobRun.class)));
        assertThat(sameJobRun.getStatus(), is(equalTo(RUNNING)));

        // delete
        jobRunRepository.delete(jobRun);
        sameJobRun = jobRunRepository.findOne(jobRun.getId());
        assertThat(sameJobRun, is(nullValue()));
    }

}
