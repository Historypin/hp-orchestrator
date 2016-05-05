package sk.eea.td.flow.activities;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;
import sk.eea.td.util.PathUtils;

public abstract class AbstractTransformActivity implements Activity {

    abstract protected Logger getLogger();

    @Value("${storage.directory}")
    private String outputDirectory;

    protected JobRun context;

    @Override
    public void execute(JobRun context) throws FlowException {
        this.context = context;

        getLogger().debug("Starting transform activity for job ID: {}", context.getId());

        try {
            final Map<ParamKey, String> paramMap = new HashMap<>();
            context.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));

//            Destination destination = Destination.valueOf(context.getJob().getTarget().name());
            final Path harvestPath = Paths.get(paramMap.get(ParamKey.HARVEST_PATH));
/*            final Path transformPath = PathUtils.createTransformRunSubdir(Paths.get(outputDirectory),
                    String.valueOf(context.getId()));*/
            final Path transformPath = getTransformPath(Paths.get(outputDirectory), String.valueOf(context.getId()));

            walkFileTree(harvestPath, transformPath);
            
            context.addReadOnlyParam(new ReadOnlyParam(ParamKey.TRANSFORM_PATH, transformPath.toAbsolutePath().toString()));

        } catch (Exception e) {
            throw new FlowException("Exception raised during transform action", e);
        } finally {
            getLogger().debug("Transform activity for job ID: {} has ended.", context.getId());
        }

    }

    protected Path getTransformPath(Path parentDir, String jobRunId) throws IOException {
        return PathUtils.createTransformRunSubdir(parentDir, jobRunId);
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    protected void walkFileTree(Path harvestPath, Path transformPath) throws IOException {
        Files.walkFileTree(harvestPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                getLogger().error(String.format("Error at accessing file '%s'. File will be skipped. Reason: ", file.toAbsolutePath().toString()), exc);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                final String filename = file.getFileName().toString();
                final String[] parts = filename.split("\\.", 2);
                if (parts.length != 2 && isEmpty(parts[1])) {
                    getLogger().warn("Filename '{}' does not follow pattern '[name].[source_type].[format]'. File will be skipped.");
                    return FileVisitResult.CONTINUE;
                }
                String source = parts[1];

                Path transformedFile = transform(source, file, transformPath, context);

                if (transformedFile != null) {
                    getLogger().debug("File '{}' has been transformed into file: '{}'", file.toString(), transformedFile.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    abstract protected Path transform(String source, Path file, Path transformPath, JobRun context) throws IOException;
}