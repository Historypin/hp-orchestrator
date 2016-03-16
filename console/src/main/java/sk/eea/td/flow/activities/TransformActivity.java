package sk.eea.td.flow.activities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;

public class TransformActivity implements Activity {

	public static final String SOURCE_DIR = "transformSourceDir";

	public static final String OUTPUT_DIR = "transformOutputDir";

	public static final String TRANSFORM = "transform";

	private Client client;

	private String muleURL;
	private String muleAPIPath;
	private String muleTransform;
	
	public TransformActivity() {
        ClientConfig clientConfig = new ClientConfig();
        this.client = ClientBuilder.newClient(clientConfig).register(MultiPartFeature.class);
	}
	
	@Override
	public void execute(JobRun context) throws FlowException {
		
		String source=context.getProperties().getProperty(SOURCE_DIR);
		String output=context.getProperties().getProperty(OUTPUT_DIR);
		String transform=context.getProperties().getProperty(TRANSFORM);

		File sourceDir = new File(source);
		File outputDir = new File(output);
		outputDir.mkdirs();
		
		if(!sourceDir.exists()){
			throw new FlowException("Source dir doesn't exist!");
		}

		WebTarget target = client.target(muleURL).path(muleAPIPath).path(muleTransform).queryParam("transformation", transform);

		for(File file : sourceDir.listFiles()){
			Response response= target.request(MediaType.APPLICATION_JSON, MediaType.TEXT_XML).post(Entity.entity(file, MediaType.TEXT_XML));
			String out= response.readEntity(String.class);
			saveFiles(out,response.getMediaType(), file.getName(), outputDir);
			response.close();
		}		
	}

	private void saveFiles(String json, MediaType mediaType, String fileNamePrefix, File outputDir) throws FlowException {
		if(MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)){
			JSONParser parser = new JSONParser();
			try {
				JSONObject obj = (JSONObject)parser.parse(json);
				JSONArray records = (JSONArray)obj.get("records");
				int i = 1;
				for(Object object : records){
					JSONObject pin = (JSONObject)((JSONObject)object).get("record");
					File pinFile = new File(outputDir,fileNamePrefix + i + ".json");
					FileWriter fileWriter = new FileWriter(pinFile);
					pin.writeJSONString(fileWriter);
					fileWriter.close();
					i++;
				}
			} catch (ParseException e) {
				throw new FlowException("Could not parse JSON", e);
			} catch (IOException e) {
				throw new FlowException("Could not write file", e);
			} 
		}
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public void setMuleURL(String muleURL) {
		this.muleURL = muleURL;
	}

	public void setMuleAPIPath(String muleAPIPath) {
		this.muleAPIPath = muleAPIPath;
	}

	public void setMuleTransform(String muleTransform) {
		this.muleTransform = muleTransform;
	}

}
