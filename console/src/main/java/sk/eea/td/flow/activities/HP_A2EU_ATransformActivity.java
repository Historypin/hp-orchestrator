package sk.eea.td.flow.activities;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.Activity;
import sk.eea.td.util.PathUtils;

@Component
public class HP_A2EU_ATransformActivity extends AbstractTransformActivity implements Activity {

	private final Logger LOG = LoggerFactory.getLogger(HP_A2EU_ATransformActivity.class);
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Override
	public String getName() {
		return "HP ann. to EU ann. transform";
	}

	@Override
	protected Logger getLogger() {
		return LOG;
	}

	@Override
	protected Path transform(String source, Path file, Path transformPath, JobRun context) throws IOException {
		JsonNode rootNode = objectMapper.readTree(file.toFile());
		for(JsonNode record: rootNode.get("items")){
			Path transformToFile = PathUtils.createUniqueFilename(transformPath, context.getJob().getTarget().getFormatCode());
			
		}
		return null;
	}

}
