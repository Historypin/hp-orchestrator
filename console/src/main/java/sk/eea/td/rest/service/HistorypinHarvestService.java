package sk.eea.td.rest.service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.impl.HPClientImpl;
import sk.eea.td.util.PathUtils;

@Component
public class HistorypinHarvestService {

    private static Logger LOG = LoggerFactory.getLogger(HistorypinHarvestService.class);

    private static final AtomicBoolean CANCELLED = new AtomicBoolean();

    @Value("${historypin.base.url}")
    private String baseURL;

    @Value("${historypin.api.key}")
    private String apiKey;

    @Value("${historypin.api.secret}")
    private String apiSecret;

    @Value("${storage.directory}")
    private String outputDirectory;

    private HPClient hpClient;

    @PostConstruct
    public void init() {
        this.hpClient = new HPClientImpl(baseURL, apiKey, apiSecret);
    }

    public Path harvest(String harvestId, String projectSlug) throws IOException, ParseException {
    	boolean nextPage = true;
    	long page = 1;
    	final Path harvestPath = PathUtils.createHarvestRunSubdir(Paths.get(outputDirectory), harvestId);
    	while(nextPage){
	        Response response = hpClient.getProjectSlug(projectSlug, page);	        
	        Path filename = PathUtils.createUniqueFilename(harvestPath, "hp_slug.json");
	        try (InputStream inputStream = response.readEntity(InputStream.class);
	    	        FileWriter outWriter = new FileWriter(filename.toFile()) ) {
	        	JSONObject object = (JSONObject)new JSONParser().parse(new InputStreamReader(inputStream));
	        	nextPage = Math.multiplyExact(Integer.valueOf(object.get("limit").toString()),page) < Integer.valueOf(object.get("count").toString()).intValue(); 
				object.writeJSONString(outWriter);
	        }
	        page++;
    	}
    	
    	for(File file : FileUtils.listFiles(harvestPath.toFile(), FileFilterUtils.suffixFileFilter("hp_slug.json"), FileFilterUtils.falseFileFilter())){    		
			try(Reader slugReader = new FileReader(file);) {
				JSONObject object = (JSONObject)new JSONParser().parse(slugReader);
				JSONArray results = (JSONArray) object.get("results");
				for(Object obj: results){
					JSONObject result = (JSONObject)obj;
					if(result.get("node_type").equals("pin") && result.get("id") != null){
						Long pinId = Long.valueOf(result.get("id").toString());
						Response response = hpClient.getPin(pinId);
						try (InputStream inputStream = response.readEntity(InputStream.class)) {
							Path filename = PathUtils.createUniqueFilename(harvestPath, "hp.json");
							FileUtils.copyInputStreamToFile(inputStream, filename.toFile());
						}
					}					
				}
			}
			file.delete();
    	}
        LOG.info("Harvesting of projectSlug: " + projectSlug + " is completed.");
        return harvestPath;
    }
}
