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

import com.fasterxml.jackson.core.type.TypeReference;
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

    @Test
    public void testFlow() throws Exception {

        //1. create a new job/jobRun
        JobRun jobRun = createJobRun();
        //2. run the flow (activities: harvest, transform), then pause the flow
        historypinOntotextFlowManager.trigger();

        //3. load the jobRun, load jsons
        jobRun = jobRunRepository.findOne(jobRun.getId());
        List<String> jsons = approvementService.load(ParamKey.TRANSFORM_PATH, jobRun);
        for (String json : jsons) {
            LOG.debug(json);
        }

        //4. save the jsons
        approvementService.save(ParamKey.TRANSFORM_PATH, jobRun, jsons);

        //resume the flow
        updateJobRun(jobRun, JobRunStatus.RESUMED);
        historypinOntotextFlowManager.trigger();
    }

    @Test(expected=ServiceException.class)
    public void testFlowChecksumChanged() throws Exception {

        //1. create a new job/jobRun
        JobRun jobRun = createJobRun();
        //2. run the flow (activities: harvest, transform), then pause the flow
        historypinOntotextFlowManager.trigger();

        //3. load the jobRun, load jsons
        jobRun = jobRunRepository.findOne(jobRun.getId());
        List<String> jsons = approvementService.load(ParamKey.TRANSFORM_PATH, jobRun);
        for (String json : jsons) {
            LOG.debug(json);
        }

        //change the content of the first file
        final Map<ParamKey, String> paramMap = new HashMap<>();
        jobRun.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));
        final Path path = Paths.get(paramMap.get(ParamKey.TRANSFORM_PATH));
        ObjectMapper objectMapper = new ObjectMapper();
        String content = jsons.get(0);
        Map<String, Object> map = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        String localFilename = (String) map.get("local_filename");
        Path targetPath = path.resolve(localFilename);
        FilesystemStorageService.save(targetPath, content + " ");

        //4. save the jsons, should throw ServiceException
        approvementService.save(ParamKey.TRANSFORM_PATH, jobRun, jsons);

        //resume the flow
        updateJobRun(jobRun, JobRunStatus.RESUMED);
        historypinOntotextFlowManager.trigger();
    }

    private JobRun createJobRun() {

        Job job = new Job();
        job.setName("test job");
        job.setSource(Connector.HISTORYPIN);
        job.setTarget(Connector.SD);
        job.addParam(new Param(ParamKey.HP_PROJECT_SLUG, "london"));
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
        jobRunRepository.save(jobRun);

        return jobRun;
    }

    private void updateJobRun(JobRun jobRun, JobRunStatus status) {

        jobRun.setStatus(status);
        jobRunRepository.save(jobRun);
    }
}
