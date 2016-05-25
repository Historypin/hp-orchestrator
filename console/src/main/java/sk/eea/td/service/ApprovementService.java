package sk.eea.td.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.impl.HPClientImpl;

@Component
public class ApprovementService {

    private HPClient hpClient;

    @Value("${historypin.user}")
    private Long userId;
    @Value("${historypin.base.url}")
    private String hpUrl;
    @Value("${historypin.api.key}")
    private String hpApiKey;
    @Value("${historypin.api.secret}")
    private String hpApiSecret;

    private static final Logger LOG = LoggerFactory.getLogger(ApprovementService.class);

    @PostConstruct
    public void init() {
        LOG.debug("ApprovementService hpUrl: {}, hpApiKey: {}, hpApiSecret: {}", hpUrl, hpApiKey, hpApiSecret);
        hpClient = new HPClientImpl(hpUrl, hpApiKey, hpApiSecret);
    }

    public List<String> load(ParamKey pathType, JobRun jobRun) throws ServiceException {

        final Map<ParamKey, String> paramMap = new HashMap<>();
        jobRun.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));
        final Path path = Paths.get(paramMap.get(pathType));
        return loadAll(path);
    }

    public void save(ParamKey pathType, JobRun jobRun, List<String> contents) throws ServiceException {

        final Map<ParamKey, String> paramMap = new HashMap<>();
        jobRun.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));
        final Path path = Paths.get(paramMap.get(pathType));

        List<ServiceException.Error> errors = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for (String content : contents) {
            Map<String, Object> map = null;
            try {
                map = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                LOG.error(ServiceException.Error.ErrorCode.FAILED_TO_PARSE_JSON_FROM_STRING.name(), e);
                errors.add(new ServiceException.Error(null,
                        ServiceException.Error.ErrorCode.FAILED_TO_PARSE_JSON_FROM_STRING));
                continue;
            }

            String checkSum = (String) map.get("checksum");

            String localFilename = (String) map.get("local_filename");
            if (localFilename == null) {
                LOG.error(ServiceException.Error.ErrorCode.INVALID_JSON.name());
                errors.add(new ServiceException.Error(null, ServiceException.Error.ErrorCode.INVALID_JSON));
                continue;
            }

            Path targetPath = path.resolve(localFilename);
            try {
                boolean checksumOK = verifyCheckSum(checkSum, targetPath);
                if (!checksumOK) {
                    LOG.error(ServiceException.Error.ErrorCode.CHECKSUM_CHANGED.name());
                    errors.add(
                            new ServiceException.Error(targetPath, ServiceException.Error.ErrorCode.CHECKSUM_CHANGED));
                    continue;
                }
            } catch (IOException e) {
                LOG.error(ServiceException.Error.ErrorCode.FAILED_TO_LOAD_FILE_FOR_CHECKSUM.name(), e);
                errors.add(new ServiceException.Error(targetPath,
                        ServiceException.Error.ErrorCode.FAILED_TO_LOAD_FILE_FOR_CHECKSUM));
                continue;
            }

            try {
                // set checksum to empty string before saving
                map.put("checksum", "");
                FilesystemStorageService.save(targetPath,
                        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
            } catch (IOException e) {
                LOG.error(ServiceException.Error.ErrorCode.FAILED_TO_SAVE_FILE.name(), e);
                errors.add(
                        new ServiceException.Error(targetPath, ServiceException.Error.ErrorCode.FAILED_TO_SAVE_FILE));
                continue;
            }
        }

        if (errors.size() > 0) {
            throw new ServiceException(errors);
        }
    }

    public void sendApproved(ParamKey pathType, JobRun jobRun) throws ServiceException {

        final Map<ParamKey, String> paramMap = new HashMap<>();
        jobRun.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));
        final Path path = Paths.get(paramMap.get(pathType));

        List<ServiceException.Error> errors = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> jsons = load(pathType, jobRun);// TODO pathType?
        for (String json : jsons) {
            Map<String, Object> map;
            try {
                map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                // TODO: log json content
                LOG.error(ServiceException.Error.ErrorCode.FAILED_TO_PARSE_JSON_FROM_STRING.name(), e);
                errors.add(new ServiceException.Error(path,
                        ServiceException.Error.ErrorCode.FAILED_TO_PARSE_JSON_FROM_STRING));
                throw new ServiceException(errors);
            }

            String localFilename = (String) map.get("local_filename");
            if (localFilename == null) {
                // TODO: log json content
                LOG.error(ServiceException.Error.ErrorCode.FAILED_TO_FIND_LOCAL_FILENAME_IN_JSON.name());
                errors.add(new ServiceException.Error(path,
                        ServiceException.Error.ErrorCode.FAILED_TO_FIND_LOCAL_FILENAME_IN_JSON));
                throw new ServiceException(errors);
            }

            Boolean approved = (Boolean) map.get("approved");
            if (Boolean.TRUE.equals(approved)) {
                // 1.
                Integer id = (Integer) map.get("id");
                String[] approvedTags = (String[]) map.get("approved_tags");
                String[] approvedPlaces = (String[]) map.get("approved_places");

                // 2. prepare HP update request
                // 3. call update() on HP client
                try {
                    //TODO process response
                    hpClient.updatePin(id, approvedTags, approvedPlaces);
                } catch (Exception e) {
                    LOG.error(ServiceException.Error.ErrorCode.CLIENT_REQUEST_FAILED.name(), e);
                    errors.add(new ServiceException.Error(path,
                            ServiceException.Error.ErrorCode.CLIENT_REQUEST_FAILED));
                    throw new ServiceException(errors);
                }

                // 4. delete file
                Path targetPath = path.resolve(localFilename);// TODO catch exception
                try {
                    FilesystemStorageService.delete(targetPath);
                } catch (IOException e) {
                    // TODO: log exception
                    LOG.error(ServiceException.Error.ErrorCode.FAILED_TO_DELETE_FILE.name(), e);
                    errors.add(new ServiceException.Error(targetPath,
                            ServiceException.Error.ErrorCode.FAILED_TO_DELETE_FILE));
                    throw new ServiceException(errors);
                }
            }
        }
    }

    public void saveAndSendApproved(ParamKey pathType, JobRun jobRun, List<String> contents) throws ServiceException {

        save(pathType, jobRun, contents);
        sendApproved(pathType, jobRun);
    }

    private static List<String> loadAll(Path path) throws ServiceException {

        ObjectMapper objectMapper = new ObjectMapper();
        List<ServiceException.Error> errors = new ArrayList<>();
        List<String> files = new ArrayList<>();
        for (File file : FileUtils.listFiles(path.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {

            try {
                String content = FilesystemStorageService.load(file.toPath());
                Map<String, Object> map = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {
                });
                map.put("checksum", FilesystemStorageService.checkSum(file.toPath()));
                files.add(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));

            } catch (Exception e) {
                LOG.error(ServiceException.Error.ErrorCode.FAILED_TO_LOAD_JSON_FROM_FILE.name(), e);
                errors.add(new ServiceException.Error(path,
                        ServiceException.Error.ErrorCode.FAILED_TO_LOAD_JSON_FROM_FILE));
            }
        }

        if (errors.size() > 0) {
            throw new ServiceException(errors);
        }

        return files;
    }

    private boolean verifyCheckSum(String checksum, Path targetPath) throws IOException {
        return checksum.equalsIgnoreCase(FilesystemStorageService.checkSum(targetPath));
    }
}
