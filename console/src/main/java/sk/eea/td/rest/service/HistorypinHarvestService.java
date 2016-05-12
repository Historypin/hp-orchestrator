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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sk.eea.td.console.model.Job;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.console.model.JobRun.JobRunResult;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.ReadOnlyParam;
import sk.eea.td.console.repository.JobRepository;
import sk.eea.td.console.repository.JobRunRepository;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.impl.HPClientImpl;
import sk.eea.td.rest.model.Connector;
import sk.eea.td.util.PathUtils;

@Component
public class HistorypinHarvestService {

    private final SimpleDateFormat DATE_FORMAT;

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
    
    public HistorypinHarvestService() {
    	DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
    	DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Autowired
	private JobRepository jobRepository;

    @Autowired
	private JobRunRepository jobRunRepository;

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
	        JSONObject object = storeJson(response, filename);
	        nextPage = Math.multiplyExact(Integer.valueOf(object.get("limit").toString()),page) < Integer.valueOf(object.get("count").toString()).intValue(); 
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
							Path filename = PathUtils.createUniqueFilename(harvestPath, Connector.HISTORYPIN.getFormatCode());
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

	private JSONObject storeJson(Response response, Path filename) throws IOException, ParseException {
		JSONObject object;
		try (InputStream inputStream = response.readEntity(InputStream.class);
		        FileWriter outWriter = new FileWriter(filename.toFile()) ) {
			object = (JSONObject)new JSONParser().parse(new InputStreamReader(inputStream));
			object.writeJSONString(outWriter);
		}
		return object;
	}

	public Path harvestAnnotation(String harvestId, String jobId, String from, String until) throws IOException, java.text.ParseException, ParseException {
    	final Path harvestPath = PathUtils.createHarvestRunSubdir(Paths.get(outputDirectory), harvestId);
		Job job = jobRepository.findOne(Long.valueOf(jobId));
		JobRun jobRun = jobRunRepository.findFirstByJobAndJobRunResultOrderByDateCreatedDesc(job, JobRunResult.OK);
		String fromLocal = from;
		String untilLocal;
		if(jobRun != null){
			String lastUntilParam = null;
			for(ReadOnlyParam param : jobRun.getReadOnlyParams()){
				if(param.getKey().equals(ParamKey.HP_UNTIL_CURRENT)){
					lastUntilParam = param.getValue();
					break;
				}
			}
			if(lastUntilParam != null){
				Calendar calendar = parseDate(lastUntilParam);
				calendar.roll(Calendar.SECOND, true);
				fromLocal=DATE_FORMAT.format(calendar.getTime());					
			}
		}
		Calendar now = GregorianCalendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		
		if(until == null || now.before(parseDate(until))){
			untilLocal = DATE_FORMAT.format(now.getTime());
		}else{
			untilLocal = until;
		}
		
		if(parseDate(fromLocal).after(parseDate(untilLocal))){
			//todo finish flow
			LOG.info("We have reached 'until' date. We are not harvesting.");
			return harvestPath;
		}
		Response response = hpClient.getAnnotations(fromLocal, untilLocal);
        Path filename = PathUtils.createUniqueFilename(harvestPath, Connector.HISTORYPIN_ANNOTATION.getFormatCode());
        JSONObject object = storeJson(response, filename);

		return harvestPath;
	}

	private Calendar parseDate(String lastUntilParam) throws java.text.ParseException {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(DATE_FORMAT.parse(lastUntilParam));
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
}
