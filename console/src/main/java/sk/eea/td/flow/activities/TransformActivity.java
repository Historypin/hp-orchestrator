package sk.eea.td.flow.activities;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import sk.eea.td.console.model.JobRun;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.File;

public class TransformActivity implements Activity {

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
		
		File sourceDir;
		File outputDir;
		String transformType;

//		WebTarget target = client.target(muleURL).path(muleAPIPath).path(muleTransform).queryParam("target", transformType).request().buildPost(Entity.entity(sourceDir));
//
//
//		for(File file : sourceDir.listFiles()){
//			target.request(MediaType.APPLICATION_OCTET_STREAM);
//		}

		
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
