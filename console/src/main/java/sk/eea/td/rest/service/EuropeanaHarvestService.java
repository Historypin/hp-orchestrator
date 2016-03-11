package sk.eea.td.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sk.eea.td.eu_client.api.EuropeanaClient;
import sk.eea.td.eu_client.impl.EuropeanaClientImpl;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class EuropeanaHarvestService {

    private static Logger LOG = LoggerFactory.getLogger(EuropeanaHarvestService.class);

    @Value("${europeana.base.url}")
    private String baseURL;

    @Value("${europeana.ws.key}")
    private String wsKey;

    @Value("${europeana.retry}")
    private Integer maxRetries;

    @Value("${europeana.retry.delay}")
    private Integer retryDelay;

    @Value("${europeana.output.directory}")
    private String outputDirectory;

    private EuropeanaClient europeanaClient;

    @PostConstruct
    public void init() {
        this.europeanaClient = new EuropeanaClientImpl(baseURL, wsKey, maxRetries, retryDelay);
    }

    public void harvest(String luceneQuery) throws IOException, InterruptedException {
        List<String> results = europeanaClient.search(luceneQuery);
        Path dir = Files.createDirectories(Paths.get(outputDirectory, String.valueOf(System.currentTimeMillis())));
        for(String result : results) {
            Path outputFile;
            do {
                 outputFile = dir.resolve(String.valueOf(System.currentTimeMillis()) + ".json");
            } while (Files.exists(outputFile));
            Files.write(outputFile, result.getBytes());
        }
        LOG.info("Harvesting of Lucene query: " + luceneQuery + " is completed.");
    }
}
