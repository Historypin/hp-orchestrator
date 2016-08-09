package sk.eea.td.rest.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.BlobReadOnlyParam;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.eu_client.api.EuropeanaClient;
import sk.eea.td.flow.HarvestResponse;
import sk.eea.td.util.PathUtils;

@Component
public class EuropeanaHarvestService {

    private static Logger LOG = LoggerFactory.getLogger(EuropeanaHarvestService.class);

    @Value("${storage.directory}")
    private String outputDirectory;

    @Autowired
    private EuropeanaClient europeanaClient;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public HarvestResponse harvest(AbstractJobRun context, Map<ParamKey, String> stringParamMap, Map<ParamKey, BlobReadOnlyParam> blobParamMap, Boolean fullSet) throws IOException, InterruptedException {
        final Path harvestPath = PathUtils.createHarvestRunSubdir(Paths.get(outputDirectory), context);
        boolean allItemsHarvested;
        if(stringParamMap.containsKey(ParamKey.EU_REST_QUERY)) {
            allItemsHarvested = harvestByQuery(harvestPath, stringParamMap, fullSet);
        } else if (blobParamMap.containsKey(ParamKey.EU_CSV_FILE)) {
            allItemsHarvested = harvestFromFile(context, harvestPath, blobParamMap, fullSet);
        } else {
            throw new IllegalStateException("Neither EU_REST_QUERY, or EU_CSV_FILE parameters were provided. Harvesting cannot continue!");
        }

        return new HarvestResponse(harvestPath, allItemsHarvested);
    }

    private boolean harvestByQuery(Path harvestPath, Map<ParamKey, String> stringParamMap, Boolean fullSet) throws IOException, InterruptedException {
        final String luceneQuery = stringParamMap.get(ParamKey.EU_REST_QUERY);
        final String facet = stringParamMap.get(ParamKey.EU_REST_FACET);
        String cursor = "*";
        while (!"".equals(cursor)) {
            String json = this.europeanaClient.harvest(luceneQuery, facet, cursor, (fullSet) ? "minimal" : "rich");

            JsonNode rootNode = objectMapper.readTree(json);

            // usage of cursor erases it
            cursor = "";

            JsonNode successNode = rootNode.get("success");
            if (successNode == null) {
                throw new IOException("Missing field 'success' in Europeana response!");
            } else {
                boolean success = successNode.asBoolean();
                if(!success) {
                    throw new IOException(String.format("Harvesting error. Europeana message: %s", rootNode.findPath("error").asText()));
                }
            }

            JsonNode cursorNode = rootNode.get("nextCursor");
            if (cursorNode != null) {
                cursor = cursorNode.textValue();
            }

            if (fullSet) {
                JsonNode items = rootNode.get("items");
                if (items != null && items.isArray()) {
                    for (Iterator<JsonNode> iterator = items.elements(); iterator.hasNext(); ) {
                        JsonNode idNode = iterator.next().get("id");
                        if (idNode != null && idNode.textValue() != null) {
                            String id = idNode.textValue();
                            json = this.europeanaClient.getRecord(id);
                            if (json == null) {
                                throw new IOException(MessageFormat.format("Europeana record with ID: {0}", id));
                            }
                            Path outputFile = PathUtils.createUniqueFilename(harvestPath, "eu.json");
                            Files.write(outputFile, json.getBytes());
                        } else {
                            throw new IOException("No \"id\" property found in europeana response");
                        }
                    }
                } else {
                    throw new IOException("No \"items\" element found in europeana response");
                }
            } else {
                Path outputFile = PathUtils.createUniqueFilename(harvestPath, "eu.json");
                Files.write(outputFile, json.getBytes());
            }
        }
        LOG.info("Harvesting of Lucene query: " + luceneQuery + " is completed.");
        return true;
    }

    private boolean harvestFromFile(AbstractJobRun context, Path harvestPath, Map<ParamKey, BlobReadOnlyParam> blobParamMap, Boolean fullSet) throws IOException, InterruptedException {
        final BlobReadOnlyParam blobReadOnlyParam = blobParamMap.get(ParamKey.EU_CSV_FILE);
        boolean allItemHarvested = true;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(blobReadOnlyParam.getBlobData())))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) { // skip empty lines
                    continue;
                } else {
                    try {
                        Response response = this.europeanaClient.getRecordWithFullResponse(line);
                        if(response.getStatus() != HttpStatus.OK.value()) {
                            allItemHarvested = false;
                            logError(context, line);
                            LOG.error("Europeana response failed for ID: {}. Actual response status: {}", line, response.getStatus());
                            continue;
                        }
                        Path outputFile = PathUtils.createUniqueFilename(harvestPath, "eu.json");
                        FileUtils.copyInputStreamToFile(response.readEntity(InputStream.class), outputFile.toFile());
                    } catch (IOException | InterruptedException e) {
                        allItemHarvested = false;
                        logError(context, line);
                        LOG.error("Europeana response failed for ID: {}. Reason:", line, e);
                        continue;
                    }
                }

            }
        }
        LOG.info("Harvesting of CSV file: " + blobReadOnlyParam.getBlobName() + " is completed.");
        return allItemHarvested;
    }

    private void logError(AbstractJobRun context, String id) {
        Log log = new Log();
        log.setJobRun(context);
        log.setLevel(Log.LogLevel.ERROR);
        log.setMessage(String.format("Could not retrieve item with id: %s from Europeana. See server error logs for details. This record will be skipped", id));
        logRepository.save(log);
    }
}
