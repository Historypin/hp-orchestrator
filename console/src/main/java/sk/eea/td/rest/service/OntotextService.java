package sk.eea.td.rest.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sk.eea.td.onto_client.api.OntoClient;
import sk.eea.td.onto_client.impl.OntoClientImpl;
import sk.eea.td.util.PathUtils;

@Component
public class OntotextService {

    private static Logger LOG = LoggerFactory.getLogger(OntotextService .class);

    @Value("${historypin.base.url}")
    private String baseURL;

    private OntoClient client;

    @PostConstruct
    public void init() {
        this.client = new OntoClientImpl(baseURL, null);
    }

    public Path harvest(String harvestId, String text) throws IOException {
        String[] extract = client.extract(text);
        LOG.info("Harvesting for harvestId: " + harvestId + " is completed.");
        return null;
    }
}
