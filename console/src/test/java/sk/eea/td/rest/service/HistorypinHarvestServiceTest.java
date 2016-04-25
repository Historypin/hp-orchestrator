package sk.eea.td.rest.service;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sk.eea.td.IntegrationTest;
import sk.eea.td.config.RESTClientsConfig;
import sk.eea.td.config.TestConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestConfig.class, RESTClientsConfig.class})
@Category(IntegrationTest.class)
public class HistorypinHarvestServiceTest {

	@Autowired
	private HistorypinHarvestService historypinHarvestService;
	
	@Before
	public void setUp() throws Exception {
	}
	
    @Value("${storage.directory}")
    private String outputDirectory;	

	@Test
	public void testHarvest() {
		try {
			historypinHarvestService.harvest("12345", "new-orleans");
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ParseException e) {
			e.printStackTrace();
			fail();
		}finally {
			try {
				FileUtils.deleteDirectory(new File(outputDirectory));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
