package sk.eea.td.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import sk.eea.td.console.model.AbstractJobRun;

public class PathUtils {

    private static final String STORE_PREFIX = "store_";
    private static final String HARVEST_PREFIX = "harvest_";
    private static final String APPROVED_STORE = "approvedStore";
    private static final String APPROVAL_STORE = "approvalStore";
    
    public static Path createHarvestRunSubdir(Path parentDir, AbstractJobRun jobRun) throws IOException {
        return createActivityStorageSubdir(parentDir, String.valueOf(jobRun.getId()), HARVEST_PREFIX+jobRun.getJob().getSource());
    }

    public static Path createStoreSubdir(Path parentDir, AbstractJobRun jobRun) throws IOException {
        return createActivityStorageSubdir(parentDir, String.valueOf(jobRun.getId()), STORE_PREFIX+jobRun.getJob().getTarget());
    }

    private static Path createActivityStorageSubdir(Path parentDir, String flowId, String separationFolderName) throws IOException {
        final Path dir = getJobRunPath(parentDir, flowId).resolve(separationFolderName);
        if(Files.exists(dir)) {
            throw new IllegalStateException(String.format("Directory %s already exists! Harvester output directory needs to be cleared or harvest ID is not unique.", dir.toString()));
        } else {
            return Files.createDirectories(dir);
        }
    }

    public static Path createUniqueFilename(Path parentDir, String extension) throws IOException {
        Path filename;
        do {
            filename = parentDir.resolve(String.format("%s-%s.%s", System.currentTimeMillis(), (int) Math.floor(Math.random() * 1000), extension));
        } while (Files.exists(filename));
        return filename;
    }    

	public static Path getJobRunPath(Path parentDir, String flowId) {
		Path jobRunPath = parentDir.resolve("job_run_".concat(flowId));
		return jobRunPath;
	}

    public static Path createApprovalSubdir(Path parentDir, AbstractJobRun jobRun) throws IOException {
        return createActivityStorageSubdir(parentDir, String.valueOf(jobRun.getId()), APPROVAL_STORE);
    }

    public static Path createApprovedSubdir(Path parentDir, AbstractJobRun jobRun) throws IOException {
        return createActivityStorageSubdir(parentDir, String.valueOf(jobRun.getId()), APPROVED_STORE);
    }

    public static Path getHarvestPath(Path parentDir, AbstractJobRun context) {
        return getJobRunPath(parentDir, String.valueOf(context.getId())).resolve(HARVEST_PREFIX+context.getJob().getSource());
    }

    public static Path getStorePath(Path parentDir, AbstractJobRun context) {
        return getJobRunPath(parentDir, String.valueOf(context.getId())).resolve(STORE_PREFIX+context.getJob().getTarget());
    }
    
    public static Path getApprovalStorePath(Path parentDir, AbstractJobRun context) {
        return getJobRunPath(parentDir, String.valueOf(context.getId())).resolve(APPROVAL_STORE);
    }    

    public static Path getApprovedStorePath(Path parentDir, AbstractJobRun context) {
        return getJobRunPath(parentDir, String.valueOf(context.getId())).resolve(APPROVED_STORE);
    }
}
