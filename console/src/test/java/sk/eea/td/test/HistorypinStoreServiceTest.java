package sk.eea.td.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sk.eea.td.IntegrationTest;
import sk.eea.td.config.RESTClientsConfig;
import sk.eea.td.config.TestConfig;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.rest.service.HistorypinStoreService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class, RESTClientsConfig.class })
public class HistorypinStoreServiceTest implements IntegrationTest {

    private static final Path PATH = Paths.get("/temp/td/job_run_452/transform/1459427075189-799.hp.json");

    @Autowired
    private HistorypinStoreService historypinStoreService;



    @Autowired
    private HPClient hpClient;

    @Test
    public void test() throws IOException {
        // parse HP collection location
        //hpClient.deleteAllPins(65543L);
//        historypinStoreService.store(25475L, PATH);
    }

}
