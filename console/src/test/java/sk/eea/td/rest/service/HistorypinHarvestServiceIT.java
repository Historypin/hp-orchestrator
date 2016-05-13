package sk.eea.td.rest.service;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sk.eea.td.IntegrationTest;
import sk.eea.td.config.DaoMockConfig;
import sk.eea.td.config.RESTClientsConfig;
import sk.eea.td.config.TestConfig;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.JobRun.JobRunResult;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestConfig.class, DaoMockConfig.class, RESTClientsConfig.class})
@Category(IntegrationTest.class)
public class HistorypinHarvestServiceIT {

	@Autowired
	private HistorypinHarvestService historypinHarvestService;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearUp(){
		try {
			FileUtils.deleteDirectory(new File(outputDirectory));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    @Value("${storage.directory}")
    private String outputDirectory;

    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private JobRunRepository jobRunRepository;
    
	@Test
	public void testHarvest() {
		try {
			historypinHarvestService.harvest("12345", "new-orleans");
		} catch (ParseException|IOException e) {
			fail(e.toString());
		}finally {
		}
	}
	
	@Test
	public void testAnnotationHarvest(){		
		Job job = new Job();
		job.setId(1l);
		JobRun jobRun = new JobRun();
		jobRun.setId(2l);
		jobRun.addReadOnlyParam(new ReadOnlyParam(ParamKey.HP_UNTIL_CURRENT, "2010-01-01T20:21:59Z"));
		
		EasyMock.expect(jobRepository.findOne(EasyMock.eq(1l))).andReturn(job).once();
		EasyMock.expect(jobRunRepository.findFirstByJobAndJobRunResultOrderByDateCreatedDesc(EasyMock.same(job), EasyMock.same(JobRunResult.OK))).andReturn(jobRun).once();
		EasyMock.replay(jobRepository,jobRunRepository);
		try {
			historypinHarvestService.harvestAnnotation("12345", "1", null, null);
		} catch (Exception e) {
			fail(e.toString());
		}
		EasyMock.verify(jobRepository, jobRunRepository);
	}
}
