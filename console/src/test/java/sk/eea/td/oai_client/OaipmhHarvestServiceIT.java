package sk.eea.td.oai_client;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;
import sk.eea.td.IntegrationTest;
import sk.eea.td.config.OaipmhConfig;
import sk.eea.td.config.TestPropertiesConfig;
import sk.eea.td.rest.model.OaipmhConfigWrapper;
import sk.eea.td.rest.service.OaipmhHarvestService;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPropertiesConfig.class, OaipmhConfig.class})
public class OaipmhHarvestServiceIT {

    private static Logger LOG = LoggerFactory.getLogger(OaipmhHarvestServiceIT.class);

    @Autowired
    private OaipmhHarvestService oaipmhHarvestService;

    private static final String BASE_URL = "http://oai.europeana.eu/oaicat/OAIHandler";

    private static final String FROM = "2015-07-15T19:12:06Z";

    private static final String UNTIL = "2016-02-11T10:00:00Z";

    private static final String SET = "07101_Ag_SK_EuropeanASNG";

    private static final String METADATA_PREFIX = "edm";

    @Test
    public void testSuccess() throws IOException, TransformerException, ParserConfigurationException, SAXException, NoSuchFieldException {
        OaipmhConfigWrapper config = new OaipmhConfigWrapper();
        config.setFrom(FROM);
        config.setUntil(UNTIL);
        config.setSet(SET);
        config.setMetadataPrefix(METADATA_PREFIX);

        Path tempDirectory = Files.createTempDirectory("oaipmh-integration-test");
        LOG.info("Created temporary directory at location: " + tempDirectory.toString());
        oaipmhHarvestService.setOutputDirectory(tempDirectory.toString());

        oaipmhHarvestService.harvest(config);

        List<Path> filesList = listFiles(tempDirectory);
        assertThat(filesList.size(), is(greaterThan(0)));

        // clean-up temp directory
        deleteOnExit(tempDirectory);
    }

    private static void deleteOnExit(Path path) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!Files.exists(path)) {
                return;
            }
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        Files.deleteIfExists(dir);
                        return super.postVisitDirectory(dir, exc);
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.deleteIfExists(file);
                        return super.visitFile(file, attrs);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private static List<Path> listFiles(Path path) throws IOException {
        List<Path> list = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    list.addAll(listFiles(entry));
                } else {
                    list.add(entry);
                }
            }
        }
        return list;
    }
}
