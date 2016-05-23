package sk.eea.td.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ParamKey;

public class ApprovementService {

    public List<String> load(ParamKey pathType, JobRun jobRun) throws ServiceException {

        final Map<ParamKey, String> paramMap = new HashMap<>();
        jobRun.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue()));
        final Path path = Paths.get(paramMap.get(pathType));
        try {
            return FilesystemStorageService.loadAll(path);
        } catch (IOException e) {
            //TODO: log error
            List<ServiceException.Error> errors = new ArrayList<>();
            errors.add(new ServiceException.Error(path, ServiceException.Error.ErrorCode.FAILED_TO_LOAD_JSON_FROM_FILE));
            throw new ServiceException(errors);
        }
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
                map = objectMapper.readValue(content, new TypeReference<Map<String,Object>>(){});
            } catch (IOException e) {
                //TODO LOG.error();
                errors.add(new ServiceException.Error(null, ServiceException.Error.ErrorCode.FAILED_TO_PARSE_JSON_FROM_STRING));
                continue;
            }

            String checkSum = (String) map.get("checksum");

            String localFilename = (String) map.get("local_filename");
            if (localFilename != null) {
                //TODO 1. verify file checksum
                Path targetPath = path.resolve(localFilename);
//TODO                verifyCheckSum(targetPath);

                //set checksum to empty string before saving
                map.put("checksum", "");
                try {
                    FilesystemStorageService.save(targetPath, content);
                } catch (IOException e) {
                    errors.add(new ServiceException.Error(targetPath, ServiceException.Error.ErrorCode.FAILED_TO_SAVE_FILE));
                    continue;
                }
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
        List<String> jsons = load(pathType, jobRun);//TODO pathType?
        for (String json : jsons) {
            Map<String, Object> map;
            try {
                map = objectMapper.readValue(json, new TypeReference<Map<String,Object>>(){});
            } catch (IOException e) {
                //TODO: log json content
                errors.add(new ServiceException.Error(path, ServiceException.Error.ErrorCode.FAILED_TO_PARSE_JSON_FROM_STRING));
                throw new ServiceException(errors);
            }

            String localFilename = (String) map.get("local_filename");
            if (localFilename == null) {
                //TODO: log json content
                errors.add(new ServiceException.Error(path, ServiceException.Error.ErrorCode.FAILED_TO_FIND_LOCAL_FILENAME_IN_JSON));
                throw new ServiceException(errors);
            }

            Boolean approved = (Boolean) map.get("approved");
            if (Boolean.TRUE.equals(approved)) {
                //1. 
                //2. prepare HP update request
                //3. call update() on HP client
                //4. delete file
                if (localFilename != null) {
                    Path targetPath = path.resolve(localFilename);//TODO catch exception
                    try {
                        FilesystemStorageService.delete(targetPath);
                    } catch (IOException e) {
                        //TODO: log exception
                        errors.add(new ServiceException.Error(targetPath, ServiceException.Error.ErrorCode.FAILED_TO_DELETE_FILE));
                        throw new ServiceException(errors);
                    }
                }
            }
        }
    }

    public void saveAndSendApproved(ParamKey pathType, JobRun jobRun, List<String> contents) throws ServiceException {

        save(pathType, jobRun, contents);
        sendApproved(pathType, jobRun);
    }
}
