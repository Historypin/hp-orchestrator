package sk.eea.td.flow.activities;

import java.io.File;
import java.io.FilenameFilter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.glassfish.jersey.client.ClientConfig;

import sk.eea.td.console.model.Process;
import sk.eea.td.flow.Activity;
import sk.eea.td.flow.FlowException;

public class TransformActivity implements Activity {

	private Client client;

	private String muleURL;
	private String muleAPIPath;
	private String muleTransform;
	
	public TransformActivity() {
        ClientConfig clientConfig = new ClientConfig();
        this.client = ClientBuilder.newClient(clientConfig);
	}
	
	@Override
	public void execute(Process context) throws FlowException {
		
		File sourceDir;
		File outputDir;
		String transformType;
		
		WebTarget target = client.target(muleURL).path(muleAPIPath).path(muleTransform).queryParam("target", transformType);
		
				
		for(File file : sourceDir.listFiles()){
			target.request(MediaType.APPLICATION_OCTET_STREAM).buildPut();
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
