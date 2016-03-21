package sk.eea.td.util;

import sk.eea.td.flow.Activity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathUtils {

    public static Path createHarvestRunSubdir(Path parentDir, String flowId) throws IOException {
        return createActivityStorageSubdir(parentDir, "job_run_", flowId, "harvest");
    }

    public static Path createTransformRunSubdir(Path parentDir, String flowId) throws IOException {
        return createActivityStorageSubdir(parentDir, "job_run_", flowId, "transform");
    }

    public static Path createActivityStorageSubdir(Path parentDir, String identificationPrefix, String flowId, String separationFolderName) throws IOException {
        final Path dir = parentDir.resolve(identificationPrefix.concat(flowId)).resolve(separationFolderName);
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
}
