package sk.eea.td.flow.activities;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;

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
			JSONObject object = new JSONObject();
			
			String hpObjectId = record.get("identifier").asText().replaceAll("^\\./", "");
			String generator = "http://www.historypin.org";
			
			JsonNode metadata = record.get("metadata");
			JsonNode annotatedBy = metadata.get("annotatedBy");
			String hpPersonType = annotatedBy.get("@type").asText("Agent").replaceAll("^foaf:", "");
			String hpPersonName = annotatedBy.get("name").asText();
			String created = metadata.get("annotatedAt").asText();
			String target = metadata.get("target").asText();
			JsonNode body = metadata.get("body");
			if(annotatedBy != null){
				JSONObject creator = new JSONObject();
				creator.put("@id", annotatedBy);
				creator.put("@type", hpPersonType);
				creator.put("name", hpPersonName);
				object.put("creator", creator);				
			}
			
			
			object.put("@context", "http://www.w3.org/ns/anno.jsonld");
			object.put("@type", "oa:Annotation");
			object.put("motivation", "tagging");
			object.put("generated", created);
			object.put("generator", generator);
			object.put("body", body);
			object.put("target", target);
			object.put("oa:equivalentTo", "https://www.historypin.org/en/item/"+hpObjectId);

			Path transformToFile = PathUtils.createUniqueFilename(transformPath, context.getJob().getTarget().getFormatCode());			
			object.write(new FileWriter(transformToFile.toFile()));
		}
		return transformPath;
	}

}
