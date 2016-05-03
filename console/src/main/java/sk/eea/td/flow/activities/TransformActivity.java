package sk.eea.td.flow.activities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import sk.eea.td.console.model.*;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;
import sk.eea.td.mapper.EuropeanaToHistorypinMapper;
import sk.eea.td.util.PathUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class TransformActivity implements Activity {

    private static final Logger LOG = LoggerFactory.getLogger(TransformActivity.class);

    @Value("${storage.directory}")
    private String outputDirectory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EuropeanaToHistorypinMapper europeanaToHistorypinMapper;

    @Autowired
    private LogRepository logRepository;

    @Override
    public void execute(JobRun context) throws FlowException {
        LOG.debug("Starting transform activity for job ID: {}", context.getId());
        try {
            final Map<ParamKey, String> paramMap = new HashMap<>();
            context.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));

            // TODO: temporary ugly solution!!!!
            List<Destination> destinations = new ArrayList<>();
            for (String s : context.getJob().getTarget().split(", ")) {
                try {
                    destinations.add(Destination.valueOf(s));
                } catch (IllegalArgumentException e) {
                    LOG.error("Transformation to this destination will be skipped.", e);
                }
            }
            final Destination destination = destinations.iterator().next(); // first destination


            final Path harvestPath = Paths.get(paramMap.get(ParamKey.HARVEST_PATH));
            final Path transformPath = PathUtils.createTransformRunSubdir(Paths.get(outputDirectory), String.valueOf(context.getId()));
            Files.walkFileTree(harvestPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    LOG.error(String.format("Error at accessing file '%s'. File will be skipped. Reason: ", file.toAbsolutePath().toString()), exc);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path harvestedFile, BasicFileAttributes attr) throws IOException {
                    final String filename = harvestedFile.getFileName().toString();
                    final String[] parts = filename.split("\\.", 2);
                    if (parts.length != 2 && isEmpty(parts[1])) {
                        LOG.warn("Filename '{}' does not follow pattern '[name].[source_type].[format]'. File will be skipped.");
                        return FileVisitResult.CONTINUE;
                    }

                    Path transformToFile = PathUtils.createUniqueFilename(transformPath, destination.getFormatCode());
                    Destination sourceType = Destination.getDestinationByFormatCode(parts[1]);
                    final String transformer = Destination.getTransformer(sourceType, destination);
                    switch (transformer) {
                        case "eu.json2hp.json":
                            if (!europeanaToHistorypinMapper.map(harvestedFile, transformToFile, paramMap)) {
                                Log log = new Log();
                                log.setJobRun(context);
                                log.setLevel(Log.LogLevel.ERROR);
                                log.setMessage(String.format("Not all pins from file '%s' were transformed successfully. See server logs for details.", harvestedFile));
                                logRepository.save(log);
                            }
                            break;
                        case "hp.json2mint.json":
                            Files.copy(harvestedFile, transformToFile);
                            LOG.debug("File '{}' has been transformed into file: '{}'", harvestedFile.toString(), transformToFile.toString());
                            break;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
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
