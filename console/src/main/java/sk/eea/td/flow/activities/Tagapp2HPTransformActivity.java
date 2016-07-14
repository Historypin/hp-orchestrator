package sk.eea.td.flow.activities;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.dto.ReviewDTO;
import sk.eea.td.hp_client.api.Pin;
import sk.eea.td.tagapp_client.PageableTagsDTO;
import sk.eea.td.tagapp_client.TagDTO;
import sk.eea.td.util.PathUtils;

public class Tagapp2HPTransformActivity extends AbstractTransformActivity {

    private static final Logger LOG = LoggerFactory.getLogger(Tagapp2HPTransformActivity.class);
    
    @Autowired
    private ObjectMapper mapper;
    

    @Override
    public String getName() {
        return Tagapp2HPTransformActivity.class.getSimpleName();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected Path transform(Connector source, Path inputFile, Path outputDir, JobRun context) throws IOException {
        if(!Connector.TAGAPP.equals(source)){
            throw new IOException(MessageFormat.format("Invalid input format: {0} for transformer: {1}", source, Tagapp2HPTransformActivity.class.getSimpleName()));
        }
        PageableTagsDTO tags = mapper.readValue(inputFile.toFile(), PageableTagsDTO.class);
        HashMap<String,ReviewDTO> pins = new HashMap<String,ReviewDTO>();
        for(TagDTO tag : tags.getTags()){
            ReviewDTO reviewDTO;
            if(!pins.containsKey(tag.getCulturalObjectExternalId())){
                reviewDTO = new ReviewDTO();
                reviewDTO.setId(Long.valueOf(tag.getCulturalObjectExternalId()));
                reviewDTO.setCaption(null);
                reviewDTO.setDescription(null);
                reviewDTO.setOriginalTags(new ArrayList<String>());
                reviewDTO.setUrl(null);
                pins.put(tag.getCulturalObjectExternalId(), reviewDTO);
            }
            reviewDTO = pins.get(tag.getCulturalObjectId());
            reviewDTO.getOriginalTags().add(tag.getValue());
        }
        
        return outputDir;
    }

}
