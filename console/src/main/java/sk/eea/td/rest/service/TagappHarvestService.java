package sk.eea.td.rest.service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.flow.FlowException;
import sk.eea.td.console.model.Connector;
import sk.eea.td.tagapp_client.PageableTagsDTO;
import sk.eea.td.tagapp_client.TagappClient;
import sk.eea.td.util.DateUtils;
import sk.eea.td.util.PathUtils;

@Component
public class TagappHarvestService {

    Logger LOG = LoggerFactory.getLogger(TagappHarvestService.class);
    
    @Value("${storage.directory}")
    private String outputDirectory;

    @Autowired
    private TagappClient tagappClient;

    @Autowired
    private JobRunRepository jobRunRepository;

    @Autowired 
    private ObjectMapper mapper;

    public Path harvest(String harvestId, String jobId, String from, String until) throws FlowException{
        JobRun jobRun = jobRunRepository.findOne(Long.valueOf(harvestId));
        Instant fromLocalInstant = Instant.from(DateUtils.SYSTEM_TIME_FORMAT.parse(from));
        Instant untilLocalInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        String batchId = String.valueOf(jobRun.getId());
        if(jobRun != null){
            String lastUntilParam = null;
            for(ReadOnlyParam param : jobRun.getReadOnlyParams()){
                if(param.getKey().equals(ParamKey.HP_UNTIL_CURRENT)){
                    lastUntilParam = param.getValue();
                    break;
                }
            }
            if(lastUntilParam != null){
                Instant lastUntil = Instant.from(DateUtils.SYSTEM_TIME_FORMAT.parse(lastUntilParam));
                fromLocalInstant = lastUntil.plusSeconds(1);
            }
        }
       
        if(until != null && !untilLocalInstant.isBefore(Instant.from(DateUtils.SYSTEM_TIME_FORMAT.parse(until)))){
            untilLocalInstant = Instant.from(DateUtils.SYSTEM_TIME_FORMAT.parse(until));
        }
        
        try {
            final Path harvestPath = PathUtils.createHarvestRunSubdir(Paths.get(outputDirectory), harvestId);
            if(fromLocalInstant.isAfter(untilLocalInstant)){
                //finish flow
                LOG.info("We have reached 'until' date. We are not harvesting.");
                return harvestPath;
            }
            Response response = tagappClient.harvestTags(
                    DateUtils.SYSTEM_TIME_FORMAT.format(fromLocalInstant), 
                    DateUtils.SYSTEM_TIME_FORMAT.format(untilLocalInstant), 
                    batchId);
            if(response == null || !response.hasEntity())
                throw new FlowException("Empty response.");
            PageableTagsDTO tags;
            tags = mapper.readValue(response.readEntity(String.class), PageableTagsDTO.class);
            if(tags == null)
                throw new FlowException("Invalid entity");
            Path filename = PathUtils.createUniqueFilename(harvestPath, Connector.TAGAPP.getFormatCode());
            FileWriter writer = new FileWriter(filename.toFile());
            writer.write(mapper.writeValueAsString(tags));
            writer.close();
            while(tags.getResumptionToken() !=""){
                response = tagappClient.harvestTags(tags.getResumptionToken());
                if(response == null || !response.hasEntity())
                    throw new FlowException("Empty response.");
                tags = mapper.readValue(response.readEntity(String.class), PageableTagsDTO.class);
                if(tags == null)
                    throw new FlowException("Invalid entity");
                filename = PathUtils.createUniqueFilename(harvestPath, Connector.TAGAPP.getFormatCode());
                writer = new FileWriter(filename.toFile());
                writer.close();
            }
            return harvestPath;
        } catch (JsonParseException | JsonMappingException e) {
            throw new FlowException("Error parsing JSON");
        } catch (IOException e) {
            throw new FlowException("Cannot harvest tagapp", e);
        } 
    }

}
