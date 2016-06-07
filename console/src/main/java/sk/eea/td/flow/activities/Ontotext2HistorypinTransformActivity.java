package sk.eea.td.flow.activities;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.dto.ReviewDTO;
import sk.eea.td.onto_client.dto.EnrichResponseDTO;
import sk.eea.td.onto_client.dto.EnrichResponseDTO.IdObject;
import sk.eea.td.rest.service.OntotextHarvestService;
import sk.eea.td.util.PathUtils;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Ontotext2HistorypinTransformActivity extends AbstractTransformActivity implements Activity {

    private static final Logger LOG = LoggerFactory.getLogger(Ontotext2HistorypinTransformActivity.class);

    @Autowired
    private OntotextHarvestService ontotextHarvestService;

    @Autowired
    private ObjectMapper objectMapper;

    private ObjectReader objectReader;

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
        this.objectReader = objectMapper.readerFor(new TypeReference<Map<String, Object>>() {});
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
    protected Path transform(String source, Path file, Path transformPath, JobRun context) throws IOException {
        LOG.debug("transforming, source: {}, file: {}, transformPath: {}", source, file, transformPath);

        final Map<String, Object> pin = objectReader.with(DeserializationFeature.USE_LONG_FOR_INTS).readValue(file.toFile());
        if (pin != null) {
            Long id = (Long) pin.get("id");
            String caption= (String) pin.get("caption");
            String description = (String) pin.get("description");
            String url = hpObjectUrl + id;

            EnrichResponseDTO resp = ontotextHarvestService.extract(String.valueOf(context.getId()), String.format("%s %s", caption, description), url);
            if (resp == null) {
                LOG.error("Null response from ontotext. File '{}' will be skipped.", file.toString());
                return null;
            }

            List<String> tags = transformTags(resp.getSubject());
            List<String> places = transformTags(resp.getSpatial());

            Path transformedFile = PathUtils.createUniqueFilename(transformPath, "hp_ot.json");
            ReviewDTO reviewDTO = new ReviewDTO();
            reviewDTO.setId(id);
            reviewDTO.setCaption(caption);
            reviewDTO.setDescription(description);
            reviewDTO.setApprovedTags(tags);
            reviewDTO.setOriginalTags(tags);
            reviewDTO.setApprovedPlaces(places);
            reviewDTO.setOriginalPlaces(places);
            reviewDTO.setUrl(url);
            reviewDTO.setLocalFilename(transformedFile.getFileName().toString());
            reviewDTO.setApproved(Boolean.TRUE);
            reviewDTO.setChecksum("");

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(transformedFile.toFile()), reviewDTO);
        }

        return transformPath;
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
}
