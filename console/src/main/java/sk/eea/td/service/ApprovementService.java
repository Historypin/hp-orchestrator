package sk.eea.td.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.AbstractJobRun.JobRunStatus;
import sk.eea.td.console.model.Connector;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.StringReadOnlyParam;
import sk.eea.td.console.model.dto.ReviewDTO;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.util.PathUtils;

@Component
public class ApprovementService {

//    private HPClient hpClient;

//    @Value("${historypin.user}")
//    private Long userId;

//    @Value("${historypin.base.url}")
//    private String hpUrl;

//    @Value("${historypin.api.key}")
//    private String hpApiKey;

//    @Value("${historypin.api.secret}")
//    private String hpApiSecret;

    @Value("${storage.directory}")
    private String outputDirectory;    
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobRunRepository jobRunRepository;

    private static final Logger LOG = LoggerFactory.getLogger(ApprovementService.class);

    @PostConstruct
    public void init() {
//        LOG.debug("ApprovementService hpUrl: {}, hpApiKey: {}, hpApiSecret: {}", hpUrl, hpApiKey, hpApiSecret);
//        hpClient = new HPClientImpl(hpUrl, hpApiKey, hpApiSecret);
    }

    public List<ReviewDTO> load(AbstractJobRun jobRun) throws ServiceException {

        Path path = getSourcePath(jobRun);
        return loadAll(path);
    }

    public void save(AbstractJobRun jobRun, List<ReviewDTO> reviews) throws ServiceException {

        Path path = getSourcePath(jobRun);
        checkJobNotClosed(jobRun, path);

        List<ServiceException.Error> errors = new ArrayList<>();
        for (ReviewDTO reviewDTO : reviews) {
            Path targetPath = path.resolve(reviewDTO.getLocalFilename());
            try {
                boolean checksumOK = verifyCheckSum(reviewDTO.getChecksum(), targetPath);
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
                reviewDTO.setChecksum("");
                FilesystemStorageService.save(targetPath, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reviewDTO));
            } catch (IOException e) {
                LOG.error(ServiceException.Error.ErrorCode.FAILED_TO_SAVE_FILE.name(), e);
                errors.add(
                        new ServiceException.Error(targetPath, ServiceException.Error.ErrorCode.FAILED_TO_SAVE_FILE));
            }
        }

        if (errors.size() > 0) {
            throw new ServiceException(errors);
        }
    }

    public void sendApproved(AbstractJobRun jobRun) throws ServiceException {

        List<ServiceException.Error> errors = new ArrayList<>();
        Path path = getSourcePath(jobRun);
        Path approvalPath;
        try {
            approvalPath = PathUtils.createApprovedSubdir(Paths.get(outputDirectory), jobRun);
        } catch (IOException e1) {
            LOG.error("Could not create target dir: {}", e1.toString());
            errors.add(new ServiceException.Error(PathUtils.getApprovedStorePath(Paths.get(outputDirectory), jobRun), ServiceException.Error.ErrorCode.FAILED_TO_CREATE_DIR));
            throw new ServiceException(errors);
        }
        LOG.debug("harvestPath: {}, transformPath: {}", approvalPath, approvalPath);

//        jobRun.addReadOnlyParam(new StringReadOnlyParam(ParamKey.APPROVED_PATH, approvalPath.toString()));
        jobRunRepository.save(jobRun);
        List<ReviewDTO> reviews = load(jobRun);
        for (ReviewDTO reviewDTO : reviews) {
            if (reviewDTO.getApproved()) {
                try {
                    Path outputFilePath = PathUtils.createUniqueFilename(approvalPath, Connector.APPROVAL_APP.getFormatCode());
                    File outputFile = outputFilePath.toFile();
                    outputFile.createNewFile(); 
                    objectMapper.writeValue(outputFile, reviewDTO);
                } catch (Exception e) {
                    LOG.error(ServiceException.Error.ErrorCode.FAILED_TO_SAVE_FILE.name(), e);
                    errors.add(new ServiceException.Error(path,
                            ServiceException.Error.ErrorCode.FAILED_TO_SAVE_FILE));
                    throw new ServiceException(errors);
                }

                // delete file
                Path targetPath = path.resolve(reviewDTO.getLocalFilename());// TODO catch exception
                try {
                    FilesystemStorageService.delete(targetPath);
                } catch (IOException e) {
                    LOG.error(ServiceException.Error.ErrorCode.FAILED_TO_DELETE_FILE.name(), e);
                    errors.add(new ServiceException.Error(targetPath,
                            ServiceException.Error.ErrorCode.FAILED_TO_DELETE_FILE));
                    throw new ServiceException(errors);
                }
            }
        }
    }

    public void saveAndSendApproved(AbstractJobRun jobRun, List<ReviewDTO> contents, Boolean finish) throws ServiceException {

        Path path = getSourcePath(jobRun);
        checkJobNotClosed(jobRun, path);

        save(jobRun, contents);
        sendApproved(jobRun);
        jobRun.setStatus(JobRunStatus.RESUMED);
        if(finish) jobRun.addReadOnlyParam(new StringReadOnlyParam(ParamKey.FINISH_FLOW, Boolean.TRUE.toString()));
        jobRunRepository.save(jobRun);
        
    }

    private List<ReviewDTO> loadAll(Path path) throws ServiceException {
        final List<ServiceException.Error> errors = new ArrayList<>();
        final List<ReviewDTO> reviews = new ArrayList<>();
        for (File file : FileUtils.listFiles(path.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            try {
                ReviewDTO reviewDTO = objectMapper.readValue(FilesystemStorageService.load(file.toPath()), ReviewDTO.class);
                reviewDTO.setChecksum(FilesystemStorageService.checkSum(file.toPath()));
                reviewDTO.setLocalFilename(path.relativize(file.toPath()).toString());
                reviews.add(reviewDTO);
            } catch (Exception e) {
                LOG.error(ServiceException.Error.ErrorCode.FAILED_TO_LOAD_JSON_FROM_FILE.name(), e);
                errors.add(new ServiceException.Error(path,
                        ServiceException.Error.ErrorCode.FAILED_TO_LOAD_JSON_FROM_FILE));
            }
        }

        if (errors.size() > 0) {
            throw new ServiceException(errors);
        }

        return reviews;
    }

    private boolean verifyCheckSum(String checksum, Path targetPath) throws IOException {
        return checksum.equalsIgnoreCase(FilesystemStorageService.checkSum(targetPath));
    }

    private void checkJobNotClosed(AbstractJobRun jobRun, Path path) throws ServiceException {

        JobRunStatus status = jobRunRepository.findOne(jobRun.getId()).getStatus();
        if (JobRunStatus.STOPPED == status || JobRunStatus.FINISHED == status || JobRunStatus.RESUMED == status) {
            List<ServiceException.Error> errors = new ArrayList<>();
            errors.add(new ServiceException.Error(path,
                    ServiceException.Error.ErrorCode.JOB_ALREADY_CLOSED));
            throw new ServiceException(errors);
        }
    }

    private Path getSourcePath(AbstractJobRun jobRun) {

//        final Map<ParamKey, String> paramMap = ParamUtils.copyStringReadOnLyParamsIntoStringParamMap(jobRun.getReadOnlyParams());
        return PathUtils.getApprovalStorePath(Paths.get(outputDirectory), jobRun);
    }
}
