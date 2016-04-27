package sk.eea.td.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sk.eea.td.config.FlowManagerTestConfig;
import sk.eea.td.config.PersistenceConfig;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.JobRun.JobRunStatus;
import sk.eea.td.console.model.User;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.console.repository.UsersRepository;
import sk.eea.td.flow.FlowManager;
import sk.eea.td.flow.ativities.SyncActivity;
import sk.eea.td.rest.model.Connector;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { FlowManagerTestConfig.class, PersistenceConfig.class})
public class FlowManagerTest {

    @Test
    public void testFlow() throws Exception {
        FlowManager testFlowManager = testFlowManager();
        Job job = createJob();
        testFlowManager.trigger();
        Thread.sleep(1000l);
        updateJob(job, JobRunStatus.RESUMED);
        Thread.sleep(1000l);
        testFlowManager.trigger();
        Thread.sleep(1000l);
        updateJob(job, JobRunStatus.RESUMED);
        Thread.sleep(1000l);
        testFlowManager.trigger();
        System.out.println(flowManager);
    }

    private Job createJob() {

        Job job = new Job();
        job.setName("test job");
        job.setSource(Connector.EUROPEANA);
        User user = usersRepository.findByUsername("admin");
        job.setUser(user);
        job = jobRepository.save(job);
        return job;
    }

    private void updateJob(Job job, JobRunStatus status) {

        job = jobRepository.findOne(job.getId());
        JobRun jobRun = job.getLastJobRun();
        jobRun.setStatus(status);
        jobRunRepository.save(jobRun);
    }

    @Bean
    SyncActivity activity1() {
        return new SyncActivity("activity1", false);
    }
    @Bean
    SyncActivity activity2() {
        return new SyncActivity("activity2", true);
    }
    @Bean
    SyncActivity activity3() {
        return new SyncActivity("activity3", false);
    }
    @Bean
    SyncActivity activity4() {
        return new SyncActivity("activity4", true);
    }

    @Bean
    public FlowManager testFlowManager() {
        List<Connector> sources = new ArrayList<>();
        sources.add(Connector.EUROPEANA);
        flowManager.setSources(sources);
        flowManager.addActivity(activity1());
        flowManager.addActivity(activity2());
        flowManager.addActivity(activity3());
        flowManager.addActivity(activity4());
        return flowManager;
    }

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobRunRepository jobRunRepository;
    @Autowired
    private ParamRepository paramRepository;
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private FlowManager flowManager;
    @Autowired
    private UsersRepository usersRepository;
}
