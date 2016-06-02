package sk.eea.td.flow.activities;

import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.flow.FlowException;
import sk.eea.td.console.model.Connector;
import sk.eea.td.rest.service.MintStoreService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestConfig.class}, initializers=SpringTestIntializer.class)
public class StoreActivityTest {
	
	@Autowired
	MintStoreService mintStoreService;
	
	@Autowired
	StoreActivity activity;
	
	@Before
	public void setUp() throws Exception {
		EasyMock.reset(mintStoreService);
	}

	@Test
	public void testMintTarget() {		 
		JobRun context = new JobRun();
		context.addReadOnlyParam(new ReadOnlyParam(ParamKey.TRANSFORM_PATH, "src/test/resources/mint"));
		context.addReadOnlyParam(new ReadOnlyParam(ParamKey.HP_API_KEY,"ddddd"));
		context.addReadOnlyParam(new ReadOnlyParam(ParamKey.HP_API_SECRET,"ddddd"));
		context.addReadOnlyParam(new ReadOnlyParam(ParamKey.HP_USER_ID,"0"));
		context.setJob(new Job());
		context.getJob().setTarget(Connector.MINT);
		Capture<Path> pathToZip = EasyMock.<Path>newCapture();
		
		try {
			EasyMock.expect(mintStoreService.store(EasyMock.capture(pathToZip))).andStubAnswer(new IAnswer<Boolean>() {
				@Override
				public Boolean answer() throws Throwable {
		            ZipFile zipFile = new ZipFile(pathToZip.getValue().toFile());
					ZipEntry entry = zipFile.entries().nextElement();
					if(entry == null || !entry.getName().equals("object1.mint.json")){
						fail("Zip file empty, or not containing file.");
					}
					zipFile.close();
					return Boolean.TRUE;
				}
			});
			EasyMock.replay(mintStoreService);
			activity.execute(context);
		} catch (FlowException e) {
			e.printStackTrace();
			fail(e.toString());
		}finally {
			if(pathToZip.getValue().toFile().exists()){
				pathToZip.getValue().toFile().delete();
			}
		}
		EasyMock.verify(mintStoreService);
	}
}
