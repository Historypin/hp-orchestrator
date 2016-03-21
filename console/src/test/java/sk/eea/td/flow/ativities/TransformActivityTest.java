package sk.eea.td.flow.ativities;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.FlowException;
import sk.eea.td.flow.activities.TransformActivity;

public class TransformActivityTest {

	private TransformActivity transformActivity;

	@Before
	public void setUp(){
		this.transformActivity = new TransformActivity();
		this.transformActivity.setMuleAPIPath("/api");
		this.transformActivity.setMuleTransform("/transformData");
		this.transformActivity.setMuleURL("http://localhost:8081/");
	}
	
	@Test
	public void testExecute() {
		JobRun jobRun = new JobRun();
		jobRun.setId(1l);
		Properties properties = new Properties();
		properties.put(TransformActivity.SOURCE_DIR, "src/test/resources/input");
		properties.put(TransformActivity.OUTPUT_DIR, "/tmp/1");
		properties.put(TransformActivity.TRANSFORM, "edm2hp");
		jobRun.setProperties(properties);
		try {
			transformActivity.execute(jobRun);
		} catch (FlowException e) {
			fail(e.toString());
		}		
	}

}
