package sk.eea.td.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import sk.eea.td.config.RESTClientsConfig;
import sk.eea.td.config.TestConfig;
import sk.eea.td.rest.service.HistorypinStoreService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class, RESTClientsConfig.class })
public class HistorypinStoreServiceTest {

    private static final Path PATH = Paths.get("/temp/td/job_run_3453/transform/1458725458816-821.hp.json");

    @Autowired
    private HistorypinStoreService historypinStoreService;

    @Test
    public void test() throws IOException {
        // parse HP collection location
        final Long projectId = 25467L;
        historypinStoreService.store(projectId, PATH);

    }

}
