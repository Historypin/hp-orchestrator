package sk.eea.td.flow.activities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.mapper.EuropeanaToHistorypinMapper;
import sk.eea.td.console.model.Connector;
import sk.eea.td.util.ParamUtils;
import sk.eea.td.util.PathUtils;

public class TransformActivity extends AbstractTransformActivity implements Activity {


    private static final Logger LOG = LoggerFactory.getLogger(TransformActivity.class);

    @Value("${storage.directory}")
    private String outputDirectory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EuropeanaToHistorypinMapper europeanaToHistorypinMapper;

    @Autowired
    private LogRepository logRepository;

    private static final String MINT_PREFIX = "{ \"results\": [";

    private static final byte[] MINT_PREFIX_BYTES = MINT_PREFIX.getBytes();

    private static final String MINT_SUFFIX = "] }";

    private static final byte[] MINT_SUFFIX_BYTES = MINT_SUFFIX.getBytes();

/*    @Override
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
                            byte[] array = new byte[1024];
                            try (
                                    InputStream fis = Files.newInputStream(harvestedFile);
                                    OutputStream fos = Files.newOutputStream(transformToFile)
                            ) {
                                int length;
                                fos.write(MINT_PREFIX_BYTES);
                                while ((length = fis.read(array)) != -1) {
                                    fos.write(array, 0, length);
                                }
                                fos.write(MINT_SUFFIX_BYTES);
                                LOG.debug("File '{}' has been transformed into file: '{}'", harvestedFile.toString(), transformToFile.toString());
                            }
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
    }*/

/*    private void walkFileTree(WebTarget target, Path harvestPath, Path transformPath, Destination destination, Map<ParamKey, String> paramMap, JobRun context) throws IOException {
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

                Path transformedFile = PathUtils.createUniqueFilename(transformPath, destination.getFormatCode());
                if ("eu.json2hp.json".equals(transformer)) { // additional transformation logic is required for EU2HP transformation
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
    }*/

    @Override
    public String getName() {
        return "Transform activity";
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected Path transform(Connector source, Path harvestedFile, Path transformPath, JobRun context) throws IOException {

        final Map<ParamKey, String> paramMap = ParamUtils.copyStringReadOnLyParamsIntoStringParamMap(context.getReadOnlyParams());

//        Path transformPath = getTransformPath(Paths.get(outputDirectory), String.valueOf(context.getId()));


//        final String transformer = String.format("%s2%s", source, destination.getFormatCode());
//        LOG.debug("Sending file '{}' for transformation with transformer {}", file.toString(), transformer);


/*        Path transformedFile = PathUtils.createUniqueFilename(transformPath, destination.getFormatCode());
        if ("eu.json2hp.json".equals(transformer)) { // additional transformation logic is required for EU2HP transformation
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
        }*/

        ////

        Path transformToFile = PathUtils.createUniqueFilename(transformPath, context.getJob().getTarget().getFormatCode());
        if(Connector.EUROPEANA.equals(context.getJob().getSource()) && Connector.HISTORYPIN.equals(context.getJob().getTarget())) {
            if (!europeanaToHistorypinMapper.map(harvestedFile, transformToFile, paramMap)) {
                Log log = new Log();
                log.setJobRun(context);
                log.setLevel(Log.LogLevel.ERROR);
                log.setMessage(String.format("Not all pins from file '%s' were transformed successfully. See server logs for details.", harvestedFile));
                logRepository.save(log);
            }
        }

        if(Connector.HISTORYPIN.equals(context.getJob().getSource()) && Connector.MINT.equals(context.getJob().getTarget())) {
            byte[] array = new byte[1024];
            try (
                    InputStream fis = Files.newInputStream(harvestedFile);
                    OutputStream fos = Files.newOutputStream(transformToFile)
            ) {
                int length;
                fos.write(MINT_PREFIX_BYTES);
                while ((length = fis.read(array)) != -1) {
                    fos.write(array, 0, length);
                }
                fos.write(MINT_SUFFIX_BYTES);
                LOG.debug("File '{}' has been transformed into file: '{}'", harvestedFile.toString(), transformToFile.toString());
            }
        }

        return transformToFile;
    }

}
