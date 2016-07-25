package sk.eea.td.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathUtils {

    public static String APPROVAL_STORE_FOLDER = "approvedStore";
    
    public static Path createHarvestRunSubdir(Path parentDir, String flowId) throws IOException {
        return createActivityStorageSubdir(parentDir, flowId, "harvest");
    }

    public static Path createTransformRunSubdir(Path parentDir, String flowId) throws IOException {
        return createActivityStorageSubdir(parentDir, flowId, "transform");
    }

    public static Path createActivityStorageSubdir(Path parentDir, String flowId, String separationFolderName) throws IOException {
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
        return parentDir.resolve("job_run_".concat(flowId));
	}
}
