package sk.eea.td.flow.activities;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import sk.eea.td.console.model.Destination;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;
import sk.eea.td.util.PathUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class TransformActivity implements Activity {

    private static final Logger LOG = LoggerFactory.getLogger(TransformActivity.class);

    private Client client;

    @Value("${mule.transform.url}")
    private String muleTransformURL;

    @Value("${storage.directory}")
    private String outputDirectory;

    public TransformActivity() {
        ClientConfig clientConfig = new ClientConfig();
        this.client = ClientBuilder.newClient(clientConfig).register(MultiPartFeature.class);
    }

    @Override
    public void execute(JobRun context) throws FlowException {
        LOG.debug("Starting transform activity for job ID: {}", context.getId());
        try {
            final Map<String, String> paramMap = new HashMap<>();
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
            final Path harvestPath = Paths.get(paramMap.get("harvestPath"));
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
                        final String transformer = String.format("%s2%s", parts[1], destination.getFormatCode());
                        LOG.debug("Sending file '{}' for transformation with transformer {}", file.toString(), transformer);
                        Response response = target.queryParam("transformation", transformer).request(MediaType.APPLICATION_JSON, MediaType.TEXT_XML).post(Entity.entity(file.toFile(), MediaType.TEXT_XML));
                        try (InputStream inputStream = response.readEntity(InputStream.class)) {
                            Path transformedFile = PathUtils.createUniqueFilename(transformPath, destination.getFormatCode());
                            Files.copy(inputStream, transformedFile);
                            LOG.debug("File '{}' has been transformed into file: '{}'", file.toString(), transformedFile.toString());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            context.addReadOnlyParam(new ReadOnlyParam("transformPath", transformPath.toAbsolutePath().toString()));
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
