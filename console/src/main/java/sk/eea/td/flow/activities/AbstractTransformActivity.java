package sk.eea.td.flow.activities;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.Connector;
import sk.eea.td.flow.FlowException;
import sk.eea.td.util.PathUtils;

public abstract class AbstractTransformActivity implements Activity {

    abstract protected Logger getLogger();

    @Value("${storage.directory}")
    private String outputDirectory;

    protected AbstractJobRun context;

    @Override
    public ActivityAction execute(AbstractJobRun context) throws FlowException {
        this.context = context;

        getLogger().debug("Starting transform activity for job ID: {}", context.getId());

        try {
//            final Map<ParamKey, String> paramMap = ParamUtils.copyStringReadOnLyParamsIntoStringParamMap(context.getReadOnlyParams());
//            Destination destination = Destination.valueOf(context.getJob().getTarget().name());
            final Path harvestPath = getSourcePath(context);
/*            final Path transformPath = PathUtils.createTransformRunSubdir(Paths.get(outputDirectory),
                    String.valueOf(context.getId()));*/
            final Path transformPath = createStorePath(Paths.get(outputDirectory), context);
//            transformPath.toFile().mkdirs();

            getLogger().debug("harvestPath: {}, transformPath: {}", harvestPath, transformPath);

            walkFileTree(harvestPath, transformPath);

//            context.addReadOnlyParam(new ReadOnlyParam(ParamKey.TRANSFORM_PATH, transformPath.toAbsolutePath().toString()));

        } catch (Exception e) {
            throw new FlowException("Exception raised during transform action", e);
        } finally {
            getLogger().debug("Transform activity for job ID: {} has ended.", context.getId());
        }
        
        if(isSleepAfter()){
        	return ActivityAction.SLEEP;
        }else{
        	return ActivityAction.CONTINUE;
        }
    }

    protected Path getSourcePath(AbstractJobRun context) {
        return PathUtils.getHarvestPath(Paths.get(outputDirectory), context);
//        return Paths.get(context.getReadOnlyParams().stream().filter(param -> param.getKey().equals(ParamKey.HARVEST_PATH)).findFirst().get().getValue());
    }

    protected Path createStorePath(Path parentDir, AbstractJobRun jobRun) throws IOException {
        return PathUtils.createStoreSubdir(parentDir, jobRun);
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

                Path transformedFile = transform(Connector.getConnectorByFormatCode(source), file, transformPath, context);

                if (transformedFile != null) {
                    getLogger().debug("File '{}' has been transformed into file: '{}'", file.toString(), transformedFile.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 
     * @param source Connector string
     * @param inputFile file to transform
     * @param outputDir path where to put files
     * @param context JobRun context
     * @return
     * @throws IOException
     * @throws Exception 
     */
    abstract protected Path transform(Connector source, Path inputFile, Path outputDir, AbstractJobRun context) throws IOException;
    
    public boolean isSleepAfter(){
    	return false;
    }
}
