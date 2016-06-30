package sk.eea.td.rest.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sk.eea.td.onto_client.api.OntoClient;
import sk.eea.td.onto_client.dto.EnrichmentDTO;
import sk.eea.td.onto_client.impl.OntoClientImpl;
import sk.eea.td.util.PathUtils;

@Component
public class OntotextHarvestService {

    private static Logger LOG = LoggerFactory.getLogger(OntotextHarvestService.class);

    @Value("${ontotext.base.url}")
    private String baseURL;

    @Value("${storage.directory}")
    private String outputDirectory;

    private OntoClient client;

    @PostConstruct
    public void init() {
        this.client = new OntoClientImpl(baseURL, null);
    }

    public Path harvest(String harvestId, String text, String uri) throws IOException {
        String respString = client.extract(text, uri);
        LOG.info("Harvesting for harvestId: " + harvestId + " is completed.");
        final Path harvestPath = PathUtils.createActivityStorageSubdir(Paths.get(outputDirectory), harvestId, "harvest_2");
        Path outputFile = PathUtils.createUniqueFilename(harvestPath, "ot.jsonld");
        Files.write(outputFile, respString.getBytes());

        return outputFile;
    }

    public EnrichmentDTO extract(String harvestId, String text, String uri) throws IOException {
        return client.extractUsingJsonLDParser(text, uri);
    }
}
