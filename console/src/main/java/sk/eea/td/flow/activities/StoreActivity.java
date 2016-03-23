package sk.eea.td.flow.activities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import sk.eea.td.console.model.Destination;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.dto.SaveResponseDTO;
import sk.eea.td.rest.service.HistorypinStoreService;
import sk.eea.td.util.LocationUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class StoreActivity implements Activity {

    private static final Logger LOG = LoggerFactory.getLogger(StoreActivity.class);

    @Value("${historypin.user}")
    private Long hpUser;

    @Autowired
    private HPClient hpClient;

    @Autowired
    private HistorypinStoreService historypinStoreService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogRepository logRepository;

    @Override
    public void execute(JobRun context) throws FlowException {
        LOG.debug("Starting store activity for job ID: {}", context.getId());
        try {
            final Map<String, String> paramMap = new HashMap<>();
            context.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));

            final Path transformPath = Paths.get(paramMap.get("transformPath"));
            Files.walkFileTree(transformPath, new SimpleFileVisitor<Path>() {
                private Long hpProjectId;

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    LOG.error(String.format("Error at accessing file '%s'. File will be skipped. Reason: ", file.toAbsolutePath().toString()), exc);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                    LOG.debug("Storing file '{}'.", file.toString());
                    final String filename = file.getFileName().toString();
                    final String[] parts = filename.split("\\.", 2);
                    if (parts.length != 2 && isEmpty(parts[1])) {
                        LOG.warn("Filename '{}' does not follow pattern '[name].[source_type].[format]'. File will be skipped.");
                        return FileVisitResult.CONTINUE;
                    }

                    final Destination destination = Destination.getDestinationByFormatCode(parts[1]);
                    switch (destination) {
                        case HP:
                            if (this.hpProjectId == null) {
                                // parse HP collection location
                                final String collectionLocation = paramMap.get("collectionLocation");
                                Pattern locationPattern = Pattern.compile(LocationUtils.HISTORY_PIN_LOCATION_PATTERN);
                                Matcher matcher = locationPattern.matcher(collectionLocation);
                                if (!matcher.matches()) {
                                    throw new IllegalArgumentException("Collection location: " + collectionLocation + " cannot be parsed!");
                                }
                                // create HP project
                                final String collectionName = paramMap.get("collectionName");
                                SaveResponseDTO response = hpClient.createProject(collectionName, hpUser, matcher.group(1), matcher.group(2), matcher.group(3));
                                // verify that project is created
                                if (!response.getErrors().isEmpty()) {
                                    throw new IllegalStateException("Could not create collection with name: " + collectionName + " in Historypin API. Reason: " + response.getErrors().toString());
                                } else if (response.getId() == null) {
                                    throw new IllegalStateException("Could not create collection with name: " + collectionName + " in Historypin API. Reason: projectId is null");
                                } else {
                                    LOG.debug("Created new project in Historypin with ID: {}.", response.getId());
                                    this.hpProjectId = response.getId();
                                }
                            }

                            if (!historypinStoreService.store(hpProjectId, file)) {
                                Log log = new Log();
                                log.setJobRun(context);
                                log.setTimestamp(new Date());
                                log.setLevel(Log.LogLevel.ERROR);
                                log.setMessage(String.format("Not all pins from file '%s' were saved successfully. See server logs for details.", file));
                                logRepository.save(log);
                            }
                            break;
                        case TAGAPP:
                        case MINT:
                        case EUROPEANA:
                        case SD:
                            throw new NotImplementedException("Store procedure for destination: " + destination + " is not implemented yet!");
                        default:
                            throw new IllegalArgumentException("There is no store procedure implemented for destination: " + destination);
                    }
                    LOG.debug("Storing of file '{}' has ended.", file.toString());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            throw new FlowException("Exception raised during store action", e);
        } finally {
            LOG.debug("Store activity for job ID: {} has ended.", context.getId());
        }
    }

    @Override
    public String getName() {
        return "Store activity";
    }
}
