package sk.eea.td.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sk.eea.td.eu_client.api.EuropeanaClient;
import sk.eea.td.eu_client.impl.EuropeanaClientImpl;
import sk.eea.td.util.PathUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class EuropeanaHarvestService {

    private static Logger LOG = LoggerFactory.getLogger(EuropeanaHarvestService.class);

    @Value("${storage.directory}")
    private String outputDirectory;

    @Autowired
    private EuropeanaClient europeanaClient;

    public Path harvest(String harvestId, String luceneQuery, String facet) throws IOException, InterruptedException {
        List<String> results = this.europeanaClient.search(luceneQuery, facet);
        final Path harvestPath = PathUtils.createHarvestRunSubdir(Paths.get(outputDirectory), harvestId);
        for(String result : results) {
            Path outputFile = PathUtils.createUniqueFilename(harvestPath, "eu.json");
            Files.write(outputFile, result.getBytes());
        }
        LOG.info("Harvesting of Lucene query: " + luceneQuery + " is completed.");
        return harvestPath;
    }
}
