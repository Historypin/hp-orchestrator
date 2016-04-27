package sk.eea.td.flow.activities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import sk.eea.td.console.model.*;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.model.HistorypinTransformDTO;
import sk.eea.td.rest.service.EuropeanaToHistorypinMapper;
import sk.eea.td.util.PathUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class TransformActivity implements Activity {

    private static final Logger LOG = LoggerFactory.getLogger(TransformActivity.class);

    private Client client;

    @Value("${mule.transform.url}")
    private String muleTransformURL;

    @Value("${storage.directory}")
    private String outputDirectory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EuropeanaToHistorypinMapper europeanaToHistorypinMapper;

    @Autowired
    private LogRepository logRepository;

    public TransformActivity() {
        ClientConfig clientConfig = new ClientConfig();
        this.client = ClientBuilder.newClient(clientConfig).register(MultiPartFeature.class);
    }

    @Override
    public void execute(JobRun context) throws FlowException {
        LOG.debug("Starting transform activity for job ID: {}", context.getId());
        try {
            final Map<ParamKey, String> paramMap = new HashMap<>();
            context.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));

            List<Destination> destinations = new ArrayList<>();
            for (String s : context.getJob().getTarget().split(", ")) {
                try {
                    destinations.add(Destination.valueOf(s));
                } catch (IllegalArgumentException e) {
                    LOG.error("Transformation to this destination will be skipped.", e);
                }
            }

            if (destinations.isEmpty()) {
                throw new IllegalStateException("There are no destinations set for this flow, therefore no transformation can be executed.");
            }

            final WebTarget target = client.target(muleTransformURL);
            final Path harvestPath = Paths.get(paramMap.get(ParamKey.HARVEST_PATH));
            final Path transformPath = PathUtils.createTransformRunSubdir(Paths.get(outputDirectory), String.valueOf(context.getId()));
            for (Destination destination : destinations) {
                Files.walkFileTree(harvestPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        LOG.error(String.format("Error at accessing file '%s'. File will be skipped. Reason: ", file.toAbsolutePath().toString()), exc);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                        final String filename = file.getFileName().toString();
                        final String[] parts = filename.split("\\.", 2);
                        if (parts.length != 2 && isEmpty(parts[1])) {
                            LOG.warn("Filename '{}' does not follow pattern '[name].[source_type].[format]'. File will be skipped.");
                            return FileVisitResult.CONTINUE;
                        }
                        
                        Destination sourceType = Destination.getDestinationByFormatCode(parts[1]);
						final String transformer = Destination.getTransformer(sourceType, destination);
                        LOG.debug("Sending file '{}' for transformation with transformer {}", file.toString(), transformer);
                        Response response = target.queryParam("transformation", transformer).request(MediaType.APPLICATION_JSON, MediaType.TEXT_XML)
                        		.post(Entity.entity(file.toFile(), sourceType.getMediaType()));
                        
                        if(response.getStatus() != 200){
                        	LOG.error(MessageFormat.format("Could not transform file: {0}", file.getFileName()));
                        	LOG.debug("Transformation failed", response.getEntity().toString());
                        	return FileVisitResult.CONTINUE;
                        }
                        Path transformedFile = PathUtils.createUniqueFilename(transformPath, destination.getFormatCode());
                        if(Destination.getTransformer(Destination.EUROPEANA, Destination.HP).equals(transformer)) { // additional transformation logic is required for EU2HP transformation
                            final HistorypinTransformDTO dto = objectMapper.readValue(response.readEntity(InputStream.class), HistorypinTransformDTO.class);

                            if (!europeanaToHistorypinMapper.map(dto, paramMap)) {
                                Log log = new Log();
                                log.setJobRun(context);
                                log.setLevel(Log.LogLevel.ERROR);
                                log.setMessage(String.format("Not all pins from file '%s' were transformed successfully. See server logs for details.", file));
                                logRepository.save(log);
                            }

                            objectMapper.writeValue(new FileOutputStream(transformedFile.toFile()), dto); // save enriched DTO object as file
                        } else {
                            try (InputStream inputStream = response.readEntity(InputStream.class)) {
                                Files.copy(inputStream, transformedFile);
                            }
                        }
                        LOG.debug("File '{}' has been transformed into file: '{}'", file.toString(), transformedFile.toString());
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            context.addReadOnlyParam(new ReadOnlyParam(ParamKey.TRANSFORM_PATH, transformPath.toAbsolutePath().toString()));
        } catch (Exception e) {
            throw new FlowException("Exception raised during transform action", e);
        } finally {
            LOG.debug("Transform activity for job ID: {} has ended.", context.getId());
        }
    }

    @Override
    public String getName() {
        return "Transform activity";
    }
}
