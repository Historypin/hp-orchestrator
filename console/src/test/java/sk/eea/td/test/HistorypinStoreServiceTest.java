package sk.eea.td.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sk.eea.td.IntegrationTest;
import sk.eea.td.config.RESTClientsConfig;
import sk.eea.td.config.TestConfig;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.api.Location;
import sk.eea.td.hp_client.api.Project;
import sk.eea.td.hp_client.dto.SaveResponseDTO;
import sk.eea.td.hp_client.impl.HPClientImpl;
import sk.eea.td.rest.service.HistorypinStoreService;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class, RESTClientsConfig.class })
public class HistorypinStoreServiceTest implements IntegrationTest {

    private static final Path PATH = Paths.get("/temp/td/job_run_1/transform/1460972240085-830.hp.json");

    private HistorypinStoreService historypinStoreService;

    private HPClient hpClient;

    @Value("${historypin.user}")
    private Long userId;

    @Value("${historypin.base.url}")
    private String hpUrl;

    @Value("${historypin.api.key}")
    private String hpApiKey;

    @Value("${historypin.api.secret}")
    private String hpApiSecret;

    @Before
    public void setup() {
        hpClient = new HPClientImpl(hpUrl, hpApiKey, hpApiSecret);
    }

    @Test
    public void test() throws IOException {
        // parse HP collection location

        //        hpClient.deleteAllProjects(65543L);
        //        hpClient.deleteAllPins(65543L);
    	historypinStoreService = HistorypinStoreService.getInstance(hpClient, userId);
        SaveResponseDTO saveResponseDTO = hpClient.createProject(userId, new Project("This is test collection", new Location(42d, 23d, 10000L)));
        historypinStoreService.storeToProject(saveResponseDTO.getId(), PATH);
    }

}
