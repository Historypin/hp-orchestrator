package sk.eea.td.test;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sk.eea.td.config.FlowConfig;
import sk.eea.td.config.PersistenceConfig;
import sk.eea.td.config.RESTClientsConfig;
import sk.eea.td.config.SecurityConfig;
import sk.eea.td.config.SwaggerConfig;
import sk.eea.td.config.TestConfig;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.JobRun.JobRunStatus;
import sk.eea.td.console.model.Param;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.User;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.UsersRepository;
import sk.eea.td.flow.FlowManager;
import sk.eea.td.rest.model.Connector;

@Ignore // TODO: fix this test
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class, FlowConfig.class, PersistenceConfig.class, RESTClientsConfig.class})
public class FlowManagerTest {

    @Test
    public void testFlow() throws Exception {
        Job job = createJob();
        historypinOntotextFlowManager.trigger();
        Thread.sleep(1000l); updateJob(job, JobRunStatus.RESUMED); Thread.sleep(1000l); 
        historypinOntotextFlowManager.trigger();
    }

    private Job createJob() {

        Job job = new Job();
        job.setName("test job");
        job.setSource(Connector.HISTORYPIN);
        job.setTarget(Connector.SD);
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

    @Autowired
    FlowManager historypinOntotextFlowManager;

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobRunRepository jobRunRepository;
    @Autowired
    private UsersRepository usersRepository;
}
