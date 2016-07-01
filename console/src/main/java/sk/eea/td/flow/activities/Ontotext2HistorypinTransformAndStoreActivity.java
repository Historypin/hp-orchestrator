package sk.eea.td.flow.activities;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.onto_client.dto.EnrichmentDTO;
import sk.eea.td.rest.service.OntotextHarvestService;

public class Ontotext2HistorypinTransformAndStoreActivity extends AbstractTransformActivity implements Activity {

    private static final Logger LOG = LoggerFactory.getLogger(Ontotext2HistorypinTransformAndStoreActivity.class);

    @Autowired
    private OntotextHarvestService ontotextHarvestService;

    @Autowired
    private HPClient hpClient;

    @Autowired
    private ObjectMapper objectMapper;

    private ObjectReader objectReader;

    @Value("${historypin.object.url}")
    private String hpObjectUrl;

    @Value("${historypin.base.url}")
    private String hpUrl;

    public Ontotext2HistorypinTransformAndStoreActivity() {
    }

    @PostConstruct
    public void init() throws JsonParseException, JsonMappingException, IOException {
        this.objectReader = objectMapper.readerFor(new TypeReference<Map<String, Object>>() {});
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

        final Map<String, Object> pin = objectReader.with(DeserializationFeature.USE_LONG_FOR_INTS).readValue(file.toFile());
        if (pin != null) {
            Long id = (Long) pin.get("id");
            String caption= (String) pin.get("caption");
            String description = (String) pin.get("description");
            String url = hpObjectUrl + id;

            EnrichmentDTO resp = ontotextHarvestService.extract(String.valueOf(context.getId()), String.format("%s %s", caption, description), url);
            if (resp == null) {
                LOG.error("Null response from ontotext. File '{}' will be skipped.", file.toString());
                return null;
            }

            List<String> tags = resp.getTags();
            List<String> places = resp.getPlaces();

            List<String> errors = hpClient.updatePin(id, tags, places);
            if (errors != null && !errors.isEmpty()) {
                for (String error : errors) {
                    LOG.warn(error);
                }
            }
        }

        return transformPath;
    }
}
