package sk.eea.td.flow.activities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.Activity;
import sk.eea.td.onto_client.dto.ExtractResponseDTO;
import sk.eea.td.onto_client.dto.ExtractResponseDTO.IdObject;
import sk.eea.td.rest.service.OntotextHarvestService;
import sk.eea.td.util.PathUtils;

public class Ontotext2HistorypinTransformActivity extends AbstractTransformActivity implements Activity {

    private static final Logger LOG = LoggerFactory.getLogger(Ontotext2HistorypinTransformActivity.class);

    @Autowired
    OntotextHarvestService ontotextHarvestService;

    @Autowired
    private ObjectMapper objectMapper;

    public Ontotext2HistorypinTransformActivity() {
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

//        Path transformPath = getTransformPath(Paths.get(outputDirectory), String.valueOf(context.getId()));
        LOG.debug("transforming: ", source, file, transformPath);

        Map<String, Object> map = objectMapper.readValue(file.toFile(), new TypeReference<Map<String,Object>>(){});
        Object results = map.get("results");
        if (results == null) {
            //FIXME:
        }
        //FIXME: add instanceof to List<Object>
        List<Object> resultsList = (List<Object>) results;
        for (Object result : resultsList) {
            if (result instanceof Map) {
                Map pin = (Map) result;
                String desc = (String) pin.get("desc");
                String url = (String) pin.get("url");
                getLogger().debug("desc, url: ", desc, url);

                if (desc == null || url == null) continue;
                String text = desc;
                String uri = url;
                ExtractResponseDTO resp = ontotextHarvestService.extract(String.valueOf(context.getId()), text, uri);

                List<String> tags = transformTags(resp.getSubject());
                pin.put("ot_tags", tags);
            }
        }

/*        final HistorypinTransformDTO transformation = objectMapper.readValue(file.toFile(),
                HistorypinTransformDTO.class);
        for (HistorypinTransformDTO.Record record : transformation.getRecords()) {

            final Pin pin = record.getPin();

            String text = pin.getDescription();
            String uri = pin.getLink();
            ExtractResponseDTO resp = ontotextHarvestService.extract(String.valueOf(context.getId()), text, uri);

            String tags = transformTags(resp.getSubject());
            pin.setTags(tags);
        }*/

        Path transformedFile = PathUtils.createUniqueFilename(transformPath, "HP.json");
        objectMapper.writeValue(new FileOutputStream(transformedFile.toFile()), map);
        return transformPath;
    }

    private List<String> transformTags(List<IdObject> objects) {
        return objects.stream().map(i -> i.getValue()).collect(Collectors.toList());
    }

    @Override
    protected Path getTransformPath(Path parentDir, String jobRunId) throws IOException {
        return PathUtils.createActivityStorageSubdir(parentDir, "job_run_", jobRunId, "transform_1");
    }
}
