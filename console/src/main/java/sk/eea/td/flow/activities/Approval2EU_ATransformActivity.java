package sk.eea.td.flow.activities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Instant;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.dto.ReviewDTO;
import sk.eea.td.util.PathUtils;

public class Approval2EU_ATransformActivity extends AbstractTransformActivity {

    private static final Logger LOG = LoggerFactory.getLogger(Approval2EU_ATransformActivity.class);

    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${europeana.generator.string}")
    private String generator;

    @Override
    protected Path transform(Connector source, Path inputFile, Path outputDir, JobRun context) throws IOException {
        if(!Connector.APPROVAL_APP.equals(source)){
            throw new IOException(MessageFormat.format("Invalid input file type: {0}. Expecting: {1}", source, Connector.APPROVAL_APP));
        }
        ReviewDTO reviewDTO = objectMapper.readValue(inputFile.toFile(), ReviewDTO.class);
        LOG.debug("Converting file {} into {} format", inputFile.toString(), Connector.EUROPEANA_ANNOTATION);
        JSONObject object = new JSONObject();

        JSONObject creator = new JSONObject();
        creator.put("type", "Person");
        creator.put("name", context.getJob().getUser().getUsername());

        object.put("creator", creator);
        object.put("@context", "http://www.w3.org/ns/anno.jsonld");
        object.put("type", "oa:Annotation");
        object.put("motivation", "tagging");
        object.put("generated", Instant.now().toString());
        object.put("generator", generator);
        object.put("target", "http://data.europeana.eu/item"+reviewDTO.getExternalId());

        for(String tag : reviewDTO.getApprovedTags()){
            object.put("bodyValue", tag);
            File outputFile = PathUtils.createUniqueFilename(outputDir, Connector.EUROPEANA_ANNOTATION.getFormatCode()).toFile();
            FileWriter writer = new FileWriter(outputFile);
            writer.write(object.toString());
            writer.close();
        }
        return outputDir;
    }
    
    @Override
    public String getName() {
        return Approval2EU_ATransformActivity.class.getSimpleName();
    }
    
    @Override
    protected Logger getLogger() {
        return Approval2EU_ATransformActivity.LOG;
    }
}
