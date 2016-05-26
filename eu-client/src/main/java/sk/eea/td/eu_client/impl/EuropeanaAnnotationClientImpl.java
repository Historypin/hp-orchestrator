package sk.eea.td.eu_client.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jodah.recurrent.RetryPolicy;
import sk.eea.td.eu_client.api.EuropeanaAnnotationClient;

public class EuropeanaAnnotationClientImpl implements EuropeanaAnnotationClient {

	private static final Logger LOG = LoggerFactory.getLogger(EuropeanaAnnotationClientImpl.class);
	
	private static final java.util.logging.Logger COMMONLOG = java.util.logging.Logger.getLogger(EuropeanaAnnotationClientImpl.class.getName());

	private String apiKey;
	
	private String apiUser;

	private String baseUrl;

	private Client client;
	
	public EuropeanaAnnotationClientImpl(String baseUrl, String apiKey2, String user) {
		this.baseUrl = baseUrl;
		this.apiKey = apiKey2;
		this.apiUser = user;
		this.client = ClientBuilder.newClient().register(new LoggingFilter(COMMONLOG, true));
	}

	@Override
	public String createAnnotation(String annotationJson) throws IOException, InterruptedException {
		WebTarget target = client.target(this.baseUrl).path("annotation/").queryParam("wskey", apiKey).queryParam("indexOnCreate", "true").queryParam("userToken", apiUser);
		Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(annotationJson, MediaType.APPLICATION_JSON));
		if(response.getStatus() == 201){
			LOG.debug("Annotation has been created.");
			return response.readEntity(String.class);
		}
		if(response.getStatus() == 401){
			throw new IOException("User not logged in. Please check user credentials in config.");
		}
		if(response.getStatus() == 403){
			throw new IOException(MessageFormat.format("User not authorized to access ''{0}''.", target.getUri()));
		}
		if(response.getStatus() == 404){
			throw new IOException(MessageFormat.format("Resource ''{0}'' not found", target.getUri()));
		}
		throw new IOException(MessageFormat.format("Other exception accessing ''{2}'': ''{0}: {1}''", response.getStatus(), response.getStatusInfo(),target.getUri()));
	}

}
