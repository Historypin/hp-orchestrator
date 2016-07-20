package sk.eea.td.flow.activities;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToLongFunction;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.config.DaoMockConfig;
import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.JobSubRun;
import sk.eea.td.console.model.dto.ReviewDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestConfig.class, DaoMockConfig.class})
public class Tagapp2HPTransformActivityTest {

    private Path outputDir;
    
    @Autowired
    ObjectMapper mapper;

    @Autowired
    private Tagapp2HPTransformActivity tagapp2hpTransformActivity;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        outputDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve(String.valueOf(System.currentTimeMillis()));
        outputDir.toFile().mkdirs();
    }

    @After
    public void tearUp() throws Exception {
        FileUtils.deleteDirectory(outputDir.toFile());
    }
    @Test
    public void testTransform() {
        JobSubRun jobSubRun = new JobSubRun();
        jobSubRun.setId(10000l);
        try {
            Path sourceFile = Paths.get(ClassLoader.getSystemResource("arttag/tags.json").toURI());
            Path inputFile = outputDir.resolve(sourceFile.getFileName());
            Files.copy(sourceFile, inputFile);
            tagapp2hpTransformActivity.transform(Connector.TAGAPP, inputFile, outputDir, jobSubRun);
            final List<Long> objects = new ArrayList<Long>();
            Files.walkFileTree(outputDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if(file.getFileName().toString().endsWith(Connector.APPROVAL_APP.getFormatCode())){                        
                        ReviewDTO dto = mapper.readValue(file.toFile(), ReviewDTO.class);
                        objects.add(dto.getId());
                        List<String> shouldContain = new ArrayList<String>();
                        Arrays.asList(new Integer[]{0,1,2}).forEach(value -> shouldContain.add("tag"+(dto.getId()+value.intValue())));
                        assertEquals(shouldContain, dto.getOriginalTags());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            objects.sort(null);
            List<Long> coIds = Arrays.asList(new Long[]{10l,20l,30l});
            assertEquals(coIds, objects);
        } catch (URISyntaxException e) {
            throw new Error("Missing input file");
        } catch (IOException e){
            fail("Exception thrown: " + e.toString());
        }
    }

}
