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
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.flow.FlowManager;
import sk.eea.td.flow.ativities.AsyncActivity;
import sk.eea.td.flow.ativities.SyncActivity;
import sk.eea.td.rest.model.Connector;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { FlowManagerTestConfig.class, PersistenceConfig.class})
public class FlowManagerTest {

    @Test
    public void testFlow() throws Exception {
        Job job = createJob();
        testFlowManager().trigger();
//        Thread.sleep(5000l);
//        testFlowManager().trigger();
        System.out.println(flowManager);
    }

    private Job createJob() {

        Job job = new Job();
        job.setName("test job");
        job.setSource(Connector.EUROPEANA);
        job = jobRepository.save(job);

        System.out.println(job);
        return job;
    }

    @Bean
    SyncActivity syncActivity1() {
        return new SyncActivity("sync1");
    }
    
    @Bean
    SyncActivity syncActivity2() {
        return new SyncActivity("sync2");
    }

    @Bean
    AsyncActivity asyncActivity1() {
        return new AsyncActivity("async1");
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

    @Bean
    public FlowManager testFlowManager() {
        List<Connector> sources = new ArrayList<>();
        sources.add(Connector.EUROPEANA);
        flowManager.setSources(sources);
        flowManager.addActivity(syncActivity1());
        flowManager.addActivity(asyncActivity1());
        flowManager.addActivity(syncActivity2());
        return flowManager;
    }
}
