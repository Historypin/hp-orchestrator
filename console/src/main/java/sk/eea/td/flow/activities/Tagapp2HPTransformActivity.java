package sk.eea.td.flow.activities;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.hp_client.api.Pin;
import sk.eea.td.tagapp_client.PageableTagsDTO;
import sk.eea.td.tagapp_client.TagDTO;

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
        HashMap<String,Pin> pins = new HashMap<String,Pin>();
        for(TagDTO tag : tags.getTags()){
            Pin pin;
            if(!pins.containsKey(tag.getCulturalObjectExternalId())){
                pin = new Pin();
                pin.setRemoteId(tag.getCulturalObjectExternalId());
//                pin.setTags(tags);
            }
        }
        return null;
    }

}
