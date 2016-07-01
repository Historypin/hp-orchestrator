package sk.eea.td.flow.activities;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.Log;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.repository.LogRepository;
import sk.eea.td.flow.FlowException;
import sk.eea.td.hp_client.api.Location;
import sk.eea.td.hp_client.impl.HPClientImpl;
import sk.eea.td.rest.model.Connector;
import sk.eea.td.rest.service.EuropeanaStoreService;
import sk.eea.td.rest.service.HistorypinStoreService;
import sk.eea.td.rest.service.MintStoreService;

public class StoreActivity implements Activity {

    private static final Logger LOG = LoggerFactory.getLogger(StoreActivity.class);

    @Value("${historypin.base.url}")
    private String hpUrl;

    @Autowired
    private MintStoreService mintStoreService;

    @Autowired
    private LogRepository logRepository;
    
    @Autowired
    private EuropeanaStoreService europeanaStoreService;

    private HistorypinStoreService historypinStoreService = null;
    
    @Override
    public ActivityAction execute(JobRun context) throws FlowException {
        LOG.debug("Starting store activity for job ID: {}", context.getId());
        try {
            final Map<ParamKey, String> paramMap = new HashMap<>();
            context.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));
            
            final Path transformPath = Paths.get(paramMap.get(ParamKey.TRANSFORM_PATH));
            LOG.debug("Transform path: " + transformPath);

            if(Connector.HISTORYPIN.equals(context.getJob().getTarget())){
            	historypinStoreService = HistorypinStoreService.getInstance(new HPClientImpl(hpUrl, paramMap.get(ParamKey.HP_API_KEY), paramMap.get(ParamKey.HP_API_SECRET)),Long.parseLong(paramMap.get(ParamKey.HP_USER_ID)));
            }
            
            File mintFile = File.createTempFile("mintData", ".zip");
            FileOutputStream mintOutputStream = new FileOutputStream(mintFile);
			final ZipOutputStream zipOutputStream = new ZipOutputStream(mintOutputStream);
            
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

                    Connector target = Connector.getConnectorByFormatCode(parts[1]);
                    switch (target) {
                        case HISTORYPIN:
                            if (this.hpProjectId == null) {
                                // get required parameters
                                final Location location = new Location(
                                        Double.parseDouble(paramMap.get(ParamKey.HP_LAT)),
                                        Double.parseDouble(paramMap.get(ParamKey.HP_LNG)),
                                        Long.parseLong(paramMap.get(ParamKey.HP_RADIUS))
                                );
                                this.hpProjectId = historypinStoreService.createProject(paramMap.get(ParamKey.HP_NAME), location);
                            }

                            if (!historypinStoreService.storeToProject(hpProjectId, file)) {
                                Log log = new Log();
                                log.setJobRun(context);
                                log.setLevel(Log.LogLevel.ERROR);
                                log.setMessage(String.format("Not all pins from file '%s' were saved successfully. See server logs for details.", file));
                                logRepository.save(log);
                            }
                            break;
                        case MINT:
                        	LOG.debug(MessageFormat.format("Adding {0} to {1}", file.getFileName(), mintFile.getName()));
                        	zipOutputStream.putNextEntry(new ZipEntry(file.getFileName().toString()));
                        	Files.copy(file, zipOutputStream);
                        	zipOutputStream.closeEntry();
                        	zipOutputStream.flush();                        	
                        	break;
                        case EUROPEANA_ANNOTATION:
                            if (!europeanaStoreService.storeAnnotation(this.hpProjectId, file)) {
                                Log log = new Log();
                                log.setJobRun(context);
                                log.setLevel(Log.LogLevel.ERROR);
                                log.setMessage(String.format("Annotations from file '%s' weren't saved successfully. See server logs for details.", file));
                                logRepository.save(log);
                            }
                            break;
                        case TAGAPP:
                        case EUROPEANA:
                        case SD:
                            throw new NotImplementedException("Store procedure for destination: " + target + " is not implemented yet!");                        	
                        default:
                            throw new IllegalArgumentException("There is no store procedure implemented for destination: " + target);
                    }
                    LOG.debug("Storing of file '{}' has ended.", file.toString());
                    return FileVisitResult.CONTINUE;
                }
                
            });
            zipOutputStream.close();
            mintOutputStream.close();
            if(Connector.MINT.equals(context.getJob().getTarget())){            	
            	ZipFile zipFile = new ZipFile(mintFile);
            	boolean notEmpty = zipFile.entries().hasMoreElements();
            	zipFile.close();
            	if(notEmpty){
            		LOG.debug(MessageFormat.format("Sending {0} into MINT", mintFile.getName()));
            		if (!mintStoreService.store(mintFile.toPath())){
            			Log log = new Log();
            			log.setJobRun(context);
            			log.setTimestamp(new Date());
            			log.setLevel(Log.LogLevel.ERROR);
            			log.setMessage(String.format("Not all pins were saved successfully. See server logs for details."));
            			logRepository.save(log);                        		
            		}
            	}else{
            		LOG.debug(MessageFormat.format("Not sending {0} into MINT because it is empty.", zipFile.getName()));            		
            	}
            }
            if(mintFile.exists()){
            	mintFile.delete();
            }
        } catch (Exception e) {
            throw new FlowException("Exception raised during store action", e);
        } finally {
            LOG.debug("Store activity for job ID: {} has ended.", context.getId());
        }
        return ActivityAction.CONTINUE;
    }

    @Override
    public String getName() {
        return "Store activity";
    }
}
