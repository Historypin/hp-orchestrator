package sk.eea.td.test;

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
import sk.eea.td.console.model.Param;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.User;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.UsersRepository;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowManager;
import sk.eea.td.flow.FlowManagerImpl;
import sk.eea.td.flow.activities.HarvestActivity;
import sk.eea.td.flow.activities.Ontotext2HistorypinTransformActivity;
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
/*        Thread.sleep(1000l);
        updateJob(job, JobRunStatus.RESUMED);
        Thread.sleep(1000l);
        testFlowManager.trigger();
        Thread.sleep(1000l);
        updateJob(job, JobRunStatus.RESUMED);
        Thread.sleep(1000l);
        testFlowManager.trigger();*/
    }

    private Job createJob() {

        Job job = new Job();
        job.setName("test job");
        job.setSource(Connector.HISTORYPIN);
        job.setTarget(Connector.ONTOTEXT);
        job.addParam(new Param(ParamKey.HP_PROJECT_SLUG, "london"));
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
    Activity ontotext2HistorypinTransformActivity() {
        return new Ontotext2HistorypinTransformActivity();
    }
    @Bean
    public Activity harvestActivity() {
        return new HarvestActivity();
    }

    @Autowired
    FlowManager flowManager;

    @Bean
    public FlowManager testFlowManager() {
/*        FlowManager flowManager = new FlowManagerImpl(Connector.HISTORYPIN, Connector.EUROPEANA);
        flowManager.addActivity(activity1());
        flowManager.addActivity(activity2());
        flowManager.addActivity(activity3());
        flowManager.addActivity(activity4());*/
        //FlowManager flowManager = new FlowManagerImpl(Connector.HISTORYPIN, Connector.ONTOTEXT);
        flowManager.setSource(Connector.HISTORYPIN);
        flowManager.setTarget(Connector.ONTOTEXT);
        flowManager.addActivity(harvestActivity());
        flowManager.addActivity(ontotext2HistorypinTransformActivity());
        return flowManager;
    }

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobRunRepository jobRunRepository;
    @Autowired
    private UsersRepository usersRepository;
}
