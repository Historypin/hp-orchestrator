package sk.eea.td.flow.activities;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.rest.model.Connector;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class HP_A2EU_ATransformActivityTest {

	@Autowired
	HP_A2EU_ATransformActivity activity;
	
	private File targetDir;
		
	@Before
	public void setUp() throws Exception {
		targetDir = File.createTempFile("test", "");
	}
	
	public void tearDown(){
		if(targetDir != null && targetDir.exists()){
			targetDir.delete();
		}
	}

	@Test
	public void test() throws URISyntaxException, JSONException, FileNotFoundException {
		File historyPinFile = new File(ClassLoader.getSystemResource("hp/test_hpan.json").toURI());
		targetDir.mkdirs();
		File europeanaFile = new File(ClassLoader.getSystemResource("europeana/test_euan.json").toURI());
		JSONObject object = new JSONObject(new JSONTokener(new FileReader(historyPinFile)));
		JobRun jobContext = new JobRun();
		try {
			activity.transform(Connector.HISTORYPIN_ANNOTATION.getFormatCode(), historyPinFile.toPath(), targetDir.toPath(), jobContext);
		} catch (IOException e) {
			fail(e.toString());
		}
		
	}

}
