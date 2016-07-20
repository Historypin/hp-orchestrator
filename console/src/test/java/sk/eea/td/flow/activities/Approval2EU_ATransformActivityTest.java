package sk.eea.td.flow.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.After;
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
import sk.eea.td.console.model.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestConfig.class, DaoMockConfig.class})
public class Approval2EU_ATransformActivityTest {
    
    @Value("${europeana.generator.string}")
    private String generator;
    
    @Autowired
    private Approval2EU_ATransformActivity approval2EU_ATransformActivity;
    private Path outputDir;
    
    @Before
    public void setUp() throws Exception {
        outputDir = Paths.get(System.getProperty("java.io.tmpdir"), "testApprovalTransform");
        outputDir.toFile().mkdirs();
    }

    @After
    public void tearUp() throws Exception {
        FileUtils.deleteDirectory(outputDir.toFile());
    }
    
    @Test
    public void test() throws Exception {
        User user = new User();
        user.setUsername("test user");
        
        Job job = new Job();
        job.setId(1l);
        job.setUser(user);
        
        JobRun jobRun = new JobRun();
        jobRun.setId(1000l);
        jobRun.setJob(job);

        Path inputFile = outputDir.resolve("input.json");
        try{
            URI uri = ClassLoader.getSystemResource("approval/tagapp.json").toURI();
            Files.copy(Paths.get(uri), inputFile);
            approval2EU_ATransformActivity.transform(Connector.APPROVAL_APP, inputFile, outputDir, jobRun);
        }catch (IOException e) {
            fail(e.toString());          
        }
        
        List<String> tags = new ArrayList<String>();
        Files.walkFileTree(outputDir, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(file.getFileName().equals(inputFile.getFileName())){
                    return FileVisitResult.CONTINUE;
                }
                JSONObject object = new JSONObject(new JSONTokener(new FileReader(file.toFile()))); 
                assertEquals("http://www.w3.org/ns/anno.jsonld", object.getString("@context"));
                assertEquals("oa:Annotation", object.getString("type"));
                assertEquals("tagging", object.getString("motivation"));
                assertEquals(generator, object.getString("generator"));
                assertEquals("http://data.europeana.eu/item/SNG_SET/P_1234", object.getString("target"));
                assertEquals("test user", object.getJSONObject("creator").getString("name"));
                assertEquals("Person", object.getJSONObject("creator").getString("type"));
                tags.add(object.getString("bodyValue"));
                return FileVisitResult.CONTINUE;
            }
        });
        tags.sort(null);
        assertEquals(Arrays.asList(new String[]{"tag1", "tag2"}), tags);
    }
    
    @Test
    public void testInvalidSourceFail(){
        try {
            approval2EU_ATransformActivity.transform(Connector.EUROPEANA, null, null, null);
            fail("Exception should be thrown");
        }catch(IOException e){
            assertTrue(e.getMessage().startsWith("Invalid input file type"));
        }        
    }

}
