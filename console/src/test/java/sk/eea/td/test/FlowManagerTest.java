package sk.eea.td.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.config.FlowConfig;
import sk.eea.td.config.PersistenceConfig;
import sk.eea.td.config.RESTClientsConfig;
import sk.eea.td.config.TestConfig;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.JobRun.JobRunStatus;
import sk.eea.td.console.model.Param;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.console.model.User;
import sk.eea.td.console.model.dto.ReviewDTO;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.ParamRepository;
import sk.eea.td.console.repository.UsersRepository;
import sk.eea.td.flow.FlowManager;
import sk.eea.td.rest.model.Connector;
import sk.eea.td.service.ApprovementService;
import sk.eea.td.service.FilesystemStorageService;
import sk.eea.td.service.ServiceException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class, FlowConfig.class, PersistenceConfig.class, RESTClientsConfig.class})
public class FlowManagerTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlowManagerTest.class);

    @Autowired
    private FlowManager historypinOntotextFlowManager;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobRunRepository jobRunRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private ParamRepository paramRepository;
    @Autowired
    private ApprovementService approvementService;

    @Ignore
    @Test
    public void testFlow() throws Exception {

        //1. create a new job/jobRun
        JobRun jobRun = createJobRun();
        //2. run the flow (activities: harvest, transform), then pause the flow
        historypinOntotextFlowManager.trigger();

        //3. load the jobRun, load jsons
        jobRun = jobRunRepository.findOne(jobRun.getId());
        List<ReviewDTO> reviews = approvementService.load(jobRun);
        for (ReviewDTO reviewDTO : reviews) {
            LOG.debug(reviewDTO.toString());
        }

        //4. save the jsons
        approvementService.save(jobRun, reviews);

        //resume the flow
        //updateJobRun(jobRun, JobRunStatus.RESUMED);
        approvementService.finish(jobRun);
        historypinOntotextFlowManager.trigger();
    }

    @Test
    public void testNewFlow2() throws Exception {

        //1. create a new job/jobRun
        JobRun jobRun = createJobRun();
        //2. run the flow (activities: harvest, transform), then pause the flow
        historypinOntotextFlowManager.trigger();
    }

    @Ignore
    @Test(expected=ServiceException.class)
    public void testFlowChecksumChanged() throws Exception {

        //1. create a new job/jobRun
        JobRun jobRun = createJobRun();
        //2. run the flow (activities: harvest, transform), then pause the flow
        historypinOntotextFlowManager.trigger();

        //3. load the jobRun, load jsons
        jobRun = jobRunRepository.findOne(jobRun.getId());
        List<ReviewDTO> reviews = approvementService.load(jobRun);
        for (ReviewDTO reviewDTO : reviews) {
            LOG.debug(reviewDTO.toString());
        }

        //change the content of the first file
        final Map<ParamKey, String> paramMap = new HashMap<>();
        jobRun.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));
        final Path path = Paths.get(paramMap.get(ParamKey.TRANSFORM_PATH));
        ObjectMapper objectMapper = new ObjectMapper();
        ReviewDTO reviewDTO = reviews.get(0);
        Path targetPath = path.resolve(reviewDTO.getLocalFilename());
        FilesystemStorageService.save(targetPath, reviewDTO.getLocalFilename()+ " ");

        //4. save the jsons, should throw ServiceException
        approvementService.save(jobRun, reviews);

        //resume the flow
        updateJobRun(jobRun, JobRunStatus.RESUMED);
        historypinOntotextFlowManager.trigger();
    }

    private JobRun createJobRun() {

        Job job = new Job();
        job.setName("test job");
        job.setSource(Connector.HISTORYPIN);
        job.setTarget(Connector.SD);
        job.addParam(new Param(ParamKey.HP_PROJECT_SLUG, "central-institue-catalogue"));
        User user = usersRepository.findByUsername("admin");
        job.setUser(user);
        job = jobRepository.save(job);

        JobRun jobRun = new JobRun();
        jobRun.setJob(job);
        jobRun.setStatus(JobRun.JobRunStatus.NEW);
        Set<Param> paramList = paramRepository.findByJob(job);
        for (Param param : paramList) {
            jobRun.addReadOnlyParam(new ReadOnlyParam(param));
        }
        return jobRunRepository.save(jobRun);
    }

    private void updateJobRun(JobRun jobRun, JobRunStatus status) {

        jobRun.setStatus(status);
        jobRunRepository.save(jobRun);
    }
}
