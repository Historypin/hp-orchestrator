package sk.eea.td.rest.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.eu_client.api.EuropeanaClient;
import sk.eea.td.util.PathUtils;

@Component
public class EuropeanaHarvestService {

    private static Logger LOG = LoggerFactory.getLogger(EuropeanaHarvestService.class);

    @Value("${storage.directory}")
    private String outputDirectory;

    @Autowired
    private EuropeanaClient europeanaClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public Path harvest(String harvestId, String luceneQuery, String facet, Boolean fullSet) throws IOException, InterruptedException {
        List<String> results = this.europeanaClient.search(luceneQuery, facet);
        final Path harvestPath = PathUtils.createHarvestRunSubdir(Paths.get(outputDirectory), harvestId);
        for(String result : results) {
            if(fullSet){
                JsonNode rootNode = objectMapper.readTree(result);
                JsonNode items = rootNode.get("items");
                if(items.isArray()){
                    for(Iterator<JsonNode> iterator = items.elements(); iterator.hasNext();){
                        JsonNode idNode = iterator.next().get("id");
                        if(idNode != null && idNode.textValue() != null){
                            String id = idNode.textValue();
                            String json = this.europeanaClient.getRecord(id);
                            if(json == null){
                                throw new IOException(MessageFormat.format("Europeana record with ID: {0}", id));
                            }
                            Path outputFile = PathUtils.createUniqueFilename(harvestPath, "eu.json");
                            Files.write(outputFile, json.getBytes());                            
                        }else{
                            throw new IOException("No \"id\" property found in europeana response");
                        }
                    }
                }else{
                    throw new IOException("No \"items\" element found in europeana response");
                }
            }else{
                Path outputFile = PathUtils.createUniqueFilename(harvestPath, "eu.json");
                Files.write(outputFile, result.getBytes());
            }
        }
        LOG.info("Harvesting of Lucene query: " + luceneQuery + " is completed.");
        return harvestPath;
    }
}
