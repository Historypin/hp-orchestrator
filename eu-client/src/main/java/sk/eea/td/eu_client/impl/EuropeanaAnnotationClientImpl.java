package sk.eea.td.eu_client.impl;

import java.io.IOException;
import java.text.MessageFormat;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.eu_client.api.EuropeanaAnnotationClient;

public class EuropeanaAnnotationClientImpl implements EuropeanaAnnotationClient {

	private static final Logger LOG = LoggerFactory.getLogger(EuropeanaAnnotationClientImpl.class);
	
	private static final java.util.logging.Logger COMMONLOG = java.util.logging.Logger.getLogger(EuropeanaAnnotationClientImpl.class.getName());

	private String apiKey;
	
	private String apiUser;

	private String baseUrl;

	private Client client;

    private ObjectMapper mapper;

	public EuropeanaAnnotationClientImpl(String baseUrl, String apiKey2, String user) {
		this.baseUrl = baseUrl;
		this.apiKey = apiKey2;
		this.apiUser = user;
		this.mapper = new ObjectMapper();
		this.client = ClientBuilder.newClient()
                .register(JacksonFeature.class)
				.register(new LoggingFilter(COMMONLOG, true));
	}

	@Override
	public String createAnnotation(String annotationJson) throws IOException, InterruptedException {
		WebTarget target = client.target(this.baseUrl).path("annotation/").queryParam("wskey", apiKey).queryParam("indexOnCreate", "true").queryParam("userToken", apiUser);
		Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(annotationJson, MediaType.APPLICATION_JSON));
		String entity = response.readEntity(String.class);
		if(response.getStatus() == 201){
		    String id = null;
		    if(entity != null){
    		    JsonNode node = mapper.readTree(entity);
    		    if(node.has("id")){
    		        id = node.get("id").asText();
    		    }
		    }
	        LOG.debug("Annotation has been created with id: {}.", id);
			return entity;
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
		LOG.debug("Response: "+ entity);
		throw new IOException(MessageFormat.format("Other exception accessing ''{2}'': ''{0}: {1}''", response.getStatus(), response.getStatusInfo(),target.getUri()));
	}

}
