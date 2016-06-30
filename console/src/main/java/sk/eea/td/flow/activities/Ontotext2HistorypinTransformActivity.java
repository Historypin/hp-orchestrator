package sk.eea.td.flow.activities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.onto_client.dto.EnrichResponseDTO;
import sk.eea.td.onto_client.dto.EnrichResponseDTO.IdObject;
import sk.eea.td.rest.model.Connector;
import sk.eea.td.rest.service.OntotextHarvestService;
import sk.eea.td.util.PathUtils;

public class Ontotext2HistorypinTransformActivity extends AbstractTransformActivity implements Activity {

    private static final Logger LOG = LoggerFactory.getLogger(Ontotext2HistorypinTransformActivity.class);
    private static final String[] NEW_PIN_PROPERTIES = new String[] {"id", "caption", "description"};

    @Autowired
    OntotextHarvestService ontotextHarvestService;

    @Autowired
    private ObjectMapper objectMapper;

//    @Value("classpath*:/efd-context-links.json")
//    private Resource contextLinksJsonResource;

    @Value("${historypin.object.url}")
    private String hpObjectUrl;

    private Set<String> contextLinks;

    public Ontotext2HistorypinTransformActivity() {
    }

    @PostConstruct
    public void init() throws JsonParseException, JsonMappingException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("efd-context-links.json");
        //contextLinks = objectMapper.readValue(contextLinksJsonResource.getInputStream(), Set.class);
        contextLinks = objectMapper.readValue(is, Set.class);
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
    protected Path transform(Connector source, Path file, Path transformPath, JobRun context) throws IOException {

        LOG.debug("transforming, source: {}, file: {}, transformPath: {}", source, file, transformPath);

        Map<String, Object> pin = objectMapper.readValue(file.toFile(), new TypeReference<Map<String, Object>>() {});
        if (pin != null) {
            String desc = (String) pin.get("description");
            //String url = (String) pin.get("link");
            Integer id = (Integer) pin.get("id");
            String url = hpObjectUrl + id;

            if (desc == null) {
                return null;
            }
            EnrichResponseDTO resp = ontotextHarvestService.extract(String.valueOf(context.getId()), desc, url);
            if (resp == null) {
                return null;
            }

            List<String> tags = transformTags(resp.getSubject());
            List<String> places = transformTags(resp.getSpatial());

            Path transformedFile = PathUtils.createUniqueFilename(transformPath, "hp_ot.json");
            Map<String, Object> newPin = createNewPin(pin);
            newPin.put("approved_tags", tags);
            newPin.put("original_tags", tags);
            newPin.put("approved_places", places);
            newPin.put("original_places", places);
            newPin.put("url", url);
            newPin.put("local_filename", transformedFile.getFileName().toString());
            newPin.put("approved", true);
            newPin.put("checksum", "");

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(transformedFile.toFile()), newPin);
        }

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
        if (objects == null) {
            return new ArrayList<String>();
        }
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

    @Override
    public boolean isSleepAfter() {
        return true;
    }
}
