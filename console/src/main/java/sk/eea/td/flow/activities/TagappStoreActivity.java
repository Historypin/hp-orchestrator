package sk.eea.td.flow.activities;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.flow.FlowException;
import sk.eea.td.rest.service.TagappStoreService;

public class TagappStoreActivity implements Activity {

    private static final Logger LOG = LoggerFactory.getLogger(TagappStoreActivity.class);

    @Autowired
    private TagappStoreService tagappStoreService;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    JobRunRepository jobRunRepository;
    
    @Override
    public ActivityAction execute(JobRun context) throws FlowException {
        final Map<ParamKey, String> paramMap = new HashMap<>();
        context.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));

        try{
            String tagappBatchId;
            if(paramMap.get(ParamKey.TAGAPP_BATCH) == null){
                tagappBatchId = tagappStoreService.createBatch();
                try{
                    Long.parseLong(tagappBatchId);
                }catch(NumberFormatException e){
                    throw new FlowException("Could not obtain batch id.");
                }
                context.addReadOnlyParam(new ReadOnlyParam(ParamKey.TAGAPP_BATCH, tagappBatchId));
            }else{
                tagappBatchId = paramMap.get(ParamKey.TAGAPP_BATCH);
            }
            
            final Path transformPath = Paths.get(paramMap.get(ParamKey.TRANSFORM_PATH));
            LOG.debug("Transform path: " + transformPath);
            Files.walkFileTree(transformPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    LOG.error(String.format("Error at accessing file '%s'. File will be skipped. Reason: ", file.toAbsolutePath().toString()), exc);
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
                  LOG.debug("Sending file {} to Tag App.", file.getFileName());
                  if(!tagappStoreService.storeCulturalObject(tagappBatchId, file)){
                      Log log = new Log();
                      log.setJobRun(context);
                      log.setLevel(Log.LogLevel.ERROR);
                      log.setMessage(String.format("Cultural object from file '%s' weren't saved successfully. See server logs for details.", file));
                      logRepository.save(log);
                  }
                    
                    return FileVisitResult.CONTINUE;
                };
            });
            
            if(!tagappStoreService.publishBatch(tagappBatchId)){
                Log log = new Log();
                log.setJobRun(context);
                log.setLevel(Log.LogLevel.ERROR);
                log.setMessage(String.format("Failed sending cultural object to TagApp. See server logs for details."));
                logRepository.save(log);
                throw new FlowException(MessageFormat.format("Enrichment for batch: {} failed", tagappBatchId));
            }
        }catch(Exception e){
            Log log = new Log();
            log.setJobRun(context);
            log.setLevel(Log.LogLevel.ERROR);
            log.setMessage(String.format("Failed sending cultural object to TagApp. See server logs for details."));
            logRepository.save(log);
            throw new FlowException(e);
        }
        return ActivityAction.CONTINUE;
    }

    @Override
    public String getName() {
        return TagappStoreService.class.getSimpleName();
    }


}
