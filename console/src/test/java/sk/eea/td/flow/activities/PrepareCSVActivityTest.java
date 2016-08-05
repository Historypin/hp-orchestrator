package sk.eea.td.flow.activities;

import static org.junit.Assert.fail;

import java.io.LineNumberReader;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.unbescape.csv.CsvEscape;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.config.DaoMockConfig;
import sk.eea.td.console.model.BlobReadOnlyParam;
import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.User;
import sk.eea.td.console.model.dto.ReviewDTO;
import sk.eea.td.util.ParamUtils;
import sk.eea.td.util.PathUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class, DaoMockConfig.class})
public class PrepareCSVActivityTest {

    @Autowired
    private PrepareCSVActivity prepareCsvActivity;

    @Value("${storage.directory}")
    private String outputDirectory;
    
    @Autowired
    private ObjectMapper objectMapper;

    private Path approvedDir;

    @Before
    public void setUp() throws Exception {
        approvedDir = Paths.get(System.getProperty("java.io.tmpdir"), "testPrepareCsv_approved");
        approvedDir.toFile().mkdirs();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(approvedDir.toFile());
    }

    @Test
    public void test() throws Exception {
        // prepare jobRun
        User user = new User();
        user.setUsername("test user");

        Job job = new Job();
        job.setId(1l);
        job.setUser(user);

        JobRun jobRun = new JobRun();
        jobRun.setId(1000l);
        jobRun.setJob(job);
        

//         prepare file for PrepareCSVActivity
        approvedDir = PathUtils.getApprovedStorePath(Paths.get(outputDirectory), jobRun);
        approvedDir.toFile().mkdirs();
        Path approvedFile = null;
        try {
            approvedFile = approvedDir.resolve("input.json");
            URI uri = ClassLoader.getSystemResource("approval/tagapp.json").toURI();
            Files.copy(Paths.get(uri), approvedFile);
        } catch (Exception e) {
            fail(e.toString());
        }


        try {
            // run PrepareCSVActivity
            prepareCsvActivity.execute(jobRun);

            // check prepared csv file
            BlobReadOnlyParam param = ParamUtils.copyBlobReadOnlyParamsBlobParamMap(jobRun.getReadOnlyParams()).get(ParamKey.EMAIL_ATTACHMENT);
            List<String> preparedLines = new LineNumberReader(new StringReader(new String(param.getBlobData(), "UTF-8"))).lines().collect(Collectors.toList());
            Assert.assertTrue("preparedLines.size() > 0", preparedLines.size() > 0);
            
            ReviewDTO reviewDTO = objectMapper.readValue(approvedFile.toFile(), ReviewDTO.class);
            String expectedLine = CsvEscape.escapeCsv(reviewDTO.getExternalId()) + "," + CsvEscape.escapeCsv(StringUtils.join(reviewDTO.getApprovedTags(), ','));
            Assert.assertEquals("prepared csv",  expectedLine, preparedLines.get(0));
            
        } catch (Exception e) {
            fail(e.toString());
        } finally {
            // delete working directory of activity
            Path outDirPath = PathUtils.getJobRunPath(Paths.get(outputDirectory), String.valueOf(jobRun.getId()));
            FileUtils.deleteDirectory(outDirPath.toFile());
        }
    }

}
