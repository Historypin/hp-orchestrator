package sk.eea.td.rest.service;

import java.io.IOException;
import java.nio.file.Path;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.tagapp_client.CulturalObjectDTO;
import sk.eea.td.tagapp_client.TagappClient;

@Component
public class TagappStoreService {

    private static final Logger LOG = LoggerFactory.getLogger(TagappStoreService.class);

    @Autowired
    TagappClient tagappClient;
    
    @Autowired
    ObjectMapper objectMapper;
    /**
     * Creates a new batch for tags.
     * @throws Exception 
     */
    public String createBatch() throws Exception {
        return tagappClient.createBatch();
    }

    /**
     * Create cultural object within TagApp.
     * @param tagappBatchId
     * @param file
     * @return
     */
    public boolean storeCulturalObject(String tagappBatchId, Path file) {
        try {
            CulturalObjectDTO coDto = objectMapper.readValue(file.toFile(), CulturalObjectDTO.class);
            Response response = tagappClient.addCulturalObject(tagappBatchId, coDto);
            if(response == null || !HttpStatus.CREATED.equals(response.getStatus())){
                LOG.error("Problem storing CulturalObjectDTO from file: {} into TagApp. Message: {}", file, response.hasEntity() ? response.readEntity(String.class) : "no message");
                return false;
            }
            return true;
        } catch (IOException e) {
            LOG.error("Could not read CulturalObjectDTO from file: {}", file);
            return false;
        } catch (Exception e) {
            LOG.error("Problem storing CulturalObjectDTO from file: {} into TagApp. Message: {}", file, e.toString());
            return false;
        }
    }

    public boolean publishBatch(String tagappBatchId) {
        Response response = tagappClient.startEnrichment(tagappBatchId);
        if(response == null || !HttpStatus.ACCEPTED.equals(response.getStatus())){
            LOG.error("Problem starting enrichment of batch: {}", tagappBatchId);
            return false;
        }
        
        return false;
    }

}
