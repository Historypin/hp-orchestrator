package sk.eea.td.flow.activities;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sk.eea.td.config.DaoMockConfig;
import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.StringReadOnlyParam;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.service.MintStoreService;
import sk.eea.td.util.PathUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestConfig.class, DaoMockConfig.class})
public class StoreActivityTest {
	
	@Autowired
	MintStoreService mintStoreService;
	
	@Autowired
	StoreActivity activity;

    @Value("${storage.directory}")
    private String outputDirectory;
	
	@Before
	public void setUp() throws Exception {
		EasyMock.reset(mintStoreService);
	}

	@Test
	public void testMintTarget() throws IOException {		 
		JobRun context = new JobRun();
		context.setId(1l);
//		context.addReadOnlyParam(new StringReadOnlyParam(ParamKey.TRANSFORM_PATH, "src/test/resources/mint"));
		context.addReadOnlyParam(new StringReadOnlyParam(ParamKey.HP_API_KEY,"ddddd"));
		context.addReadOnlyParam(new StringReadOnlyParam(ParamKey.HP_API_SECRET,"ddddd"));
		context.addReadOnlyParam(new StringReadOnlyParam(ParamKey.HP_USER_ID,"0"));
		context.setJob(new Job());
		context.getJob().setTarget(Connector.MINT);
        FileUtils.copyDirectory(new File("src/test/resources/mint"), PathUtils.getStorePath(Paths.get(outputDirectory), context).toFile());
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
