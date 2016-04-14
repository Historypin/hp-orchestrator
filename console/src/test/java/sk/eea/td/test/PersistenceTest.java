package sk.eea.td.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sk.eea.td.IntegrationTest;
import sk.eea.td.config.PersistenceConfig;
import sk.eea.td.config.RESTClientsConfig;
import sk.eea.td.config.TestConfig;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.LogRepository;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class, PersistenceConfig.class, RESTClientsConfig.class})
public class PersistenceTest implements IntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRunRepository jobRunRepository;

    @Autowired
    private LogRepository logRepository;

    @Test
    public void testLog() throws Exception {

//        Job job = jobRepository.findByOrderByIdDesc().iterator().next();
//        JobRun jobRun = jobRunRepository.findTopByJobOrderByIdDesc(job);
//
//        Log log = new Log(new Date(), Log.LogLevel.ERROR, "thi sis horrible message", jobRun);
//        log = logRepository.save(log);
//
//        System.out.println(log.toString());

        System.out.println(logRepository.findAllRelevantLogs());
    }

    @Test
    public void testJob() throws Exception {

        Job job = new Job();
        job.setName("test job");
        job = jobRepository.save(job);

        JobRun jobRun = new JobRun();
        jobRun.setJob(job);
        jobRun = jobRunRepository.save(jobRun);

        for (int i = 0; i < 10; i++) {
            jobRun.addReadOnlyParam(new ReadOnlyParam("test" + i, "balh"));
            jobRun = jobRunRepository.save(jobRun);
        }


        System.out.println(jobRun);

    }
}
