package sk.eea.td.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.ParamKey;

public class FilesystemStorageService {

    public static String load(Path path) throws IOException {
        // TODO null path
        return new String(Files.readAllBytes(path));
    }

    public static void save(Path path, String content) throws IOException {
        // TODO null content
        Files.write(path, content.getBytes());
    }

    public static List<String> loadAll(Path path) throws IOException {

        List<String> files = new ArrayList<>();
        for (File file : FileUtils.listFiles(path.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            String content = load(file.toPath());
            files.add(content);
        }
        return files;
    }

    public static void delete(Path path) throws IOException {
        Files.delete(path);
    }

    public static String checkSum(Path path) throws IOException {
        return DigestUtils.md5Hex(Files.newInputStream(path));
    }

    /*
     * public static List<String> loadAll(ParamKey pathType, JobRun jobRun) throws IOException { final Map<ParamKey,
     * String> paramMap = new HashMap<>(); jobRun.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(),
     * p.getValue())); final Path path = Paths.get(paramMap.get(pathType)); return loadAll(path); }
     */

    /*
     * public static void saveAll(ParamKey pathType, JobRun jobRun, List<String> contents) throws JsonParseException,
     * JsonMappingException, IOException { final Map<ParamKey, String> paramMap = new HashMap<>();
     * jobRun.getReadOnlyParams().stream().forEach(p -> paramMap.put(p.getKey(), p.getValue())); final Path path =
     * Paths.get(paramMap.get(pathType)); ObjectMapper objectMapper = new ObjectMapper(); for (String content :
     * contents) { Map<String, Object> map = objectMapper.readValue(content, new TypeReference<Map<String,Object>>(){});
     * String localFilename = (String) map.get("local_filename"); if (localFilename != null) { Path targetPath =
     * path.resolve(localFilename); save(targetPath, content); } } }
     */
}
