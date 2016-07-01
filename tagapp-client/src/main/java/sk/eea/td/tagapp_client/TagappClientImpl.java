package sk.eea.td.tagapp_client;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.jodah.recurrent.Recurrent;
import net.jodah.recurrent.RetryPolicy;

public class TagappClientImpl implements TagappClient {

    private final static Logger LOG = LoggerFactory.getLogger(TagappClientImpl.class);

    private final Client client;

    private final String baseURL;

    private final ObjectMapper objectMapper;

    private final RetryPolicy retryPolicy;

    public TagappClientImpl(String baseUrl, String username, String password) {
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(username, password);
        this.baseURL = baseUrl;
        this.objectMapper = new ObjectMapper();
        this.retryPolicy = new RetryPolicy()
                .retryOn(failure -> failure instanceof ProcessingException)
                .withBackoff(2, 30, TimeUnit.SECONDS)
                .withMaxRetries(3);
        ClientConfig clientConfig = new ClientConfig();
        this.client = ClientBuilder.newClient(clientConfig)
                .register(JacksonFeature.class)
                .register(feature)
                .register(new LoggingFilter(java.util.logging.Logger.getLogger(TagappClientImpl.class.getName()), true));
    }
    
    @Override
    public Response addTag(TagDTO tagDto) throws Exception {
        try {
            WebTarget target = client.target(baseURL).path("/api/tag/add");
            Invocation invocation = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                    .buildPost(Entity.entity(objectMapper.writeValueAsString(tagDto), MediaType.APPLICATION_JSON));
            return Recurrent.with(retryPolicy).get(() -> invocation.invoke()) ;
        }catch(JsonProcessingException e){
            throw new Exception("Could not convert tag to JSON.");
        }
    }

    @Override
    public Response addCulturalObject(String batchId, CulturalObjectDTO culturalObject) throws Exception {
        try {
            WebTarget target = client.target(baseURL).path("/api/cultural/add").path("/").path(batchId);
            Invocation invocation = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).buildPost(
                    Entity.entity(objectMapper.writeValueAsString(culturalObject), MediaType.APPLICATION_JSON));
            return Recurrent.with(retryPolicy).get(() -> invocation.invoke());
        }catch(JsonProcessingException e){
            throw new Exception("Could not convert tag to JSON.");
        }
    }

    @Override
    public Response stopEnrichment(String batchId) {
        WebTarget target = client.target(baseURL).path("/api/batch/remove/").path(batchId);
        Invocation invocation = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).buildDelete();
        return Recurrent.with(retryPolicy).get(() -> invocation.invoke());
    }

    @Override
    public Response harvestTags(String fromDate, String untilDate, String batchId) {
        WebTarget target = client.target(baseURL).path("/api/tag/list").queryParam("batchId", batchId)
                .queryParam("from", fromDate).queryParam("untilDate", untilDate);
        Invocation invocation = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).buildGet();
        return Recurrent.with(retryPolicy).get(() -> invocation.invoke());
    }

    @Override
    public Response harvestTags(String resumptionToken) {
        WebTarget target = client.target(baseURL).path("/api/tag/listNext").queryParam("resumptionToken", resumptionToken);
        Invocation invocation = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).buildGet();
        return Recurrent.with(retryPolicy).get(() -> invocation.invoke());
    }

    @Override
    public Response startEnrichment(String batchId){
        WebTarget target = client.target(baseURL).path("/api/batch/publish/").path(batchId);
        Invocation invocation = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).buildGet();
        return Recurrent.with(retryPolicy).get(() -> invocation.invoke());
    }

    @Override
    public String createBatch() throws Exception {
        WebTarget target = client.target(baseURL).path("/api/batch/create");
        Invocation invocation = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).buildPost(Entity.text(""));
        Response response = Recurrent.with(retryPolicy).get(() -> invocation.invoke());
        if(response == null || response.getStatus() != 201 || !response.hasEntity()){
            LOG.error("Error creating batch: {}", response.hasEntity() ? response.readEntity(String.class) : "no message");
            throw new Exception("Error creating a batch");
        }
        ResultMessageDTO result = objectMapper.readValue(response.readEntity(String.class), ResultMessageDTO.class);
        String batchId = result != null && result.getMessage() != null ? result.getMessage().replaceAll("id: (\\d+)$", "$1") : null;
        if(batchId == null){
            LOG.error("Error creating batch. Batch ID not found in response");
            throw new Exception("Error creating a batch");            
        }
        return batchId;
    }    
}
