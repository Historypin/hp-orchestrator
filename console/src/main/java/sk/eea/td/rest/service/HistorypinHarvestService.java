package sk.eea.td.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.impl.HPClientImpl;
import sk.eea.td.util.PathUtils;

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

    @Value("${storage.directory}")
    private String outputDirectory;

    private HPClient hpClient;

    @PostConstruct
    public void init() {
        this.hpClient = new HPClientImpl(baseURL, apiKey, apiSecret);
    }

    public Path harvest(String harvestId, String projectSlug) throws IOException {
        Response response = hpClient.getPins(projectSlug);
        final Path harvestPath = PathUtils.createHarvestRunSubdir(Paths.get(outputDirectory), harvestId);
        try (InputStream inputStream = response.readEntity(InputStream.class)) {
            Path filename = PathUtils.createUniqueFilename(harvestPath, "hp.json");
            Files.copy(inputStream, filename);
        }
        LOG.info("Harvesting of projectSlug: " + projectSlug + " is completed.");
        return harvestPath;
    }
}
