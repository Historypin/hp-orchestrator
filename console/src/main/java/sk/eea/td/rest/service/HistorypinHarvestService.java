package sk.eea.td.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.impl.HPClientImpl;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class HistorypinHarvestService {

    private static Logger LOG = LoggerFactory.getLogger(HistorypinHarvestService.class);

    private static final AtomicBoolean CANCELLED = new AtomicBoolean();

    @Value("${historypin.base.url}")
    private String baseURL;

    @Value("${historypin.api.key}")
    private String apiKey;

    @Value("${historypin.api.secret}")
    private String apiSecret;

    @Value("${historypin.output.directory}")
    private String outputDirectory;

    private HPClient hpClient;

    @PostConstruct
    public void init() {
        this.hpClient = new HPClientImpl(baseURL, apiKey, apiSecret);
    }

    public void harvest(String projectSlug) throws IOException {
        Response response = hpClient.getPins(projectSlug);
        try (InputStream inputStream = response.readEntity(InputStream.class)) {
            Path dir = Files.createDirectories(Paths.get(outputDirectory, String.valueOf(System.currentTimeMillis())));
            Files.copy(inputStream, dir.resolve(String.valueOf(System.currentTimeMillis()) + ".json"));
        }
        LOG.info("Harvesting of projectSlug: " + projectSlug + " is completed.");
    }
}
