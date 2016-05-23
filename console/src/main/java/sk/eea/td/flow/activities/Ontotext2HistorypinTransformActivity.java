package sk.eea.td.flow.activities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.Activity;
import sk.eea.td.onto_client.dto.EnrichResponseDTO;
import sk.eea.td.onto_client.dto.EnrichResponseDTO.IdObject;
import sk.eea.td.rest.service.OntotextHarvestService;
import sk.eea.td.service.FilesystemStorageService;
import sk.eea.td.util.PathUtils;

public class Ontotext2HistorypinTransformActivity extends AbstractTransformActivity implements Activity {

    private static final Logger LOG = LoggerFactory.getLogger(Ontotext2HistorypinTransformActivity.class);
    private static final String[] NEW_PIN_PROPERTIES = new String[] {"id", "caption", "description"};

    @Autowired
    OntotextHarvestService ontotextHarvestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("classpath:efd-context-links.json")
    private Resource contextLinksJsonResource;

    private Set<String> contextLinks;

    public Ontotext2HistorypinTransformActivity() {
    }

    @PostConstruct
    public void init() throws JsonParseException, JsonMappingException, IOException {
        contextLinks = objectMapper.readValue(contextLinksJsonResource.getFile(), Set.class);
    }

    @Override
    public String getName() {
        return "Ontotext2HistorypinTransformActivity";
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected Path transform(String source, Path file, Path transformPath, JobRun context) throws IOException {

        // Path transformPath = getTransformPath(Paths.get(outputDirectory), String.valueOf(context.getId()));
        LOG.debug("transforming, source: {}, file: {}, transformPath: {}", source, file, transformPath);

        Map<String, Object> pin = objectMapper.readValue(file.toFile(), new TypeReference<Map<String, Object>>() {
        });
        if (pin != null) {
            String desc = (String) pin.get("description");
            String url = (String) pin.get("link");
            Integer id = (Integer) pin.get("id");
            String URL = "http://www.historypin.org/en/api/pin/get.json?id=%s";
            String uri = String.format(URL, id);
            LOG.debug("desc: {}, url: {}", desc, uri);

            // FIXME
            if (desc == null /* || url == null */)
                return null;
            String text = desc;
            EnrichResponseDTO resp = ontotextHarvestService.extract(String.valueOf(context.getId()), text, uri);

            List<String> tags = transformTags(resp.getSubject());

            Path transformedFile = PathUtils.createUniqueFilename(transformPath, "HP_OT.json");
            Map<String, Object> newPin = createNewPin(pin);
            newPin.put("approved_tags", tags);
            newPin.put("original_tags", tags);
            newPin.put("url", "http://v77-beta-3.historypin-hrd.appspot.com/en/explore/pin/" + id);
            newPin.put("local_filename", transformedFile.getFileName());
            newPin.put("approved", true);
            newPin.put("checksum", "");

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(transformedFile.toFile()), newPin);
        }

        /*
         * final HistorypinTransformDTO transformation = objectMapper.readValue(file.toFile(),
         * HistorypinTransformDTO.class); for (HistorypinTransformDTO.Record record : transformation.getRecords()) {
         * final Pin pin = record.getPin(); String text = pin.getDescription(); String uri = pin.getLink();
         * ExtractResponseDTO resp = ontotextHarvestService.extract(String.valueOf(context.getId()), text, uri); String
         * tags = transformTags(resp.getSubject()); pin.setTags(tags); }
         */

        return transformPath;
    }

    private Map<String, Object> createNewPin(Map<String, Object> old) {
        Map<String, Object> newPin = new HashMap<>();
        for (String property : NEW_PIN_PROPERTIES) {
            newPin.put(property, old.get(property));
        }
        return newPin;
    }

    private List<String> transformTags(List<IdObject> objects) {
        return objects.stream().map(new Function<IdObject, String>() {
            @Override
            public String apply(IdObject idObject) {
                String value = idObject.getValue();
                return stripLinkFromValue(value);
            }
        }).collect(Collectors.toList());
    }

    private String stripLinkFromValue(String value) {
        for (String contextLink : contextLinks) {
            if (value.startsWith(contextLink)) {
                return value.substring(contextLink.length());
            }
        }
        return null;
    }

/*    @Override
    protected Path getTransformPath(Path parentDir, String jobRunId) throws IOException {
        return PathUtils.createActivityStorageSubdir(parentDir, "job_run_", jobRunId, "transform_1");
    }*/

    @Override
    public boolean isSleepAfter() {
        return true;
    }
}
