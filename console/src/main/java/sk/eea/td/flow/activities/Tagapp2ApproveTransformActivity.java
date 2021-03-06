package sk.eea.td.flow.activities;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.dto.ReviewDTO;
import sk.eea.td.tagapp_client.PageableTagsDTO;
import sk.eea.td.tagapp_client.TagDTO;
import sk.eea.td.util.PathUtils;

public class Tagapp2ApproveTransformActivity extends AbstractTransformActivity {

    private static final Logger LOG = LoggerFactory.getLogger(Tagapp2ApproveTransformActivity.class);
    
    @Autowired
    private ObjectMapper mapper;
    

    @Override
    public String getName() {
        return Tagapp2ApproveTransformActivity.class.getSimpleName();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected Path transform(Connector source, Path inputFile, Path outputDir, AbstractJobRun context) throws IOException {
        Boolean error = Boolean.FALSE;
        if(!Connector.TAGAPP.equals(source)){
            throw new IOException(MessageFormat.format("Invalid input format: {0} for transformer: {1}", source, Tagapp2ApproveTransformActivity.class.getSimpleName()));
        }
        PageableTagsDTO tags = mapper.readValue(inputFile.toFile(), PageableTagsDTO.class);
        HashMap<String,ReviewDTO> pins = new HashMap<String,ReviewDTO>();
        for(TagDTO tag : tags.getTags()){
            ReviewDTO reviewDTO;
            if(!pins.containsKey(tag.getCulturalObjectExternalId())){
                reviewDTO = new ReviewDTO();
                reviewDTO.setId(Long.valueOf(tag.getCulturalObjectId()));
                reviewDTO.setExternalId(tag.getCulturalObjectExternalId());
                reviewDTO.setCaption(tag.getCulturalObjectDescription());
                reviewDTO.setDescription("");
                reviewDTO.setOriginalTags(new ArrayList<String>());
                reviewDTO.setUrl(tag.getCulturalObjectExternalUrl());
                pins.put(tag.getCulturalObjectExternalId().toString(), reviewDTO);
            }
            reviewDTO = pins.get(tag.getCulturalObjectExternalId());
            reviewDTO.getOriginalTags().add(tag.getValue());
            reviewDTO.getApprovedTags().add(tag.getValue());
            reviewDTO.setApproved(Boolean.TRUE);
        }
        for(Entry<String, ReviewDTO> entry: pins.entrySet()){
            Path outputFile = outputDir.resolve(String.valueOf(context.getId())).resolve(entry.getValue().getId() + "."+ Connector.APPROVAL_APP.getFormatCode());
            outputFile.getParent().toFile().mkdirs();
            if(entry.getKey() == null || entry.getValue() == null || entry.getValue().getOriginalTags() == null){
                LOG.error(MessageFormat.format("Key, value or tags is empty. Key {0}, value {1}, tags {2}.", entry.getKey(), entry.getValue(), entry.getValue() != null? entry.getValue().getOriginalTags():"null"));
                error = Boolean.TRUE;
                continue;
            }
            if(!outputFile.toFile().exists()){
                outputFile.toFile().createNewFile();
                FileWriter writer = new FileWriter(outputFile.toFile());
                writer.write(mapper.writeValueAsString(entry.getValue()));
                writer.close();
            }else{
                ReviewDTO dto = mapper.readValue(outputFile.toFile(), ReviewDTO.class);
                if(dto.getId().equals(entry.getValue().getId())){
                    entry.getValue().getOriginalTags().removeAll(dto.getOriginalTags());
                    dto.getOriginalTags().addAll(entry.getValue().getOriginalTags());
                    FileWriter writer = new FileWriter(outputFile.toFile());
                    writer.write(mapper.writeValueAsString(dto));
                    writer.close();
                }else{
                    LOG.error(MessageFormat.format("More objects {0},{1}match one review file {2}", dto.getId(), entry.getValue().getId(), outputFile.getFileName()));
                    error = Boolean.TRUE;
                    continue;
                }
            }
        }
        if(error){
            throw new IOException("There was problem transforming files. Please review log files.");
        }
        return outputDir;
    }

    @Override
    protected Path createStorePath(Path parentDir, AbstractJobRun jobRun) throws IOException {
        return PathUtils.createApprovalSubdir(parentDir, jobRun);
    }
}
