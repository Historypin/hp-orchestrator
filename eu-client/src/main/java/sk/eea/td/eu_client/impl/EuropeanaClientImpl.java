package sk.eea.td.eu_client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.eea.td.eu_client.api.EuropeanaClient;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class EuropeanaClientImpl implements EuropeanaClient {

    protected static Logger LOG = LoggerFactory.getLogger(EuropeanaClientImpl.class);

    private final Client client;

    private final String baseURL;

    private final String wskey;

    private final Integer maxRetries;

    private final Integer retryDelay;

    private final ObjectMapper objectMapper;

    public EuropeanaClientImpl(String baseURL, String wskey, Integer maxRetries, Integer retryDelay) {
        this.baseURL = baseURL;
        this.wskey = wskey;
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
        this.objectMapper = new ObjectMapper();
        ClientConfig clientConfig = new ClientConfig();
        this.client = ClientBuilder.newClient(clientConfig);
    }

    @Override
    public String getRecord(String id) throws IOException, InterruptedException {
        final WebTarget target = client.target(baseURL).path("api").path("v2").path("record").path(id.concat(".json"))
                .queryParam("wskey", wskey);
        final Response response = target.request().get();
        return response.readEntity(String.class);
    }

    @Override
    public List<String> search(String luceneQuery) throws IOException, InterruptedException {
        return this.search(luceneQuery, null);
    }

    @Override public List<String> search(String luceneQuery, String facet) throws IOException, InterruptedException {
        String cursor = "*"; // initial cursor value
        int retryCounter = 0;
        List<String> harvestedJsons = new ArrayList<>();
        while (!"".equals(cursor)) {
            WebTarget target = null;
            try {
                target = client.target(baseURL).path("api").path("v2").path("search.json")
                        .queryParam("wskey", wskey)
                        .queryParam("profile", "rich")
                        .queryParam("query", luceneQuery)
                        .queryParam("cursor", cursor);

                if(isNotEmpty(facet)) {
                    target.queryParam("facet", facet);
                }

                Response response = target.request().get();
                String json = response.readEntity(String.class);

                JsonNode rootNode = objectMapper.readTree(json);

                // usage of cursor should erase it
                cursor = "";

                JsonNode cursorNode = rootNode.get("nextCursor");
                if (cursorNode != null) {
                    cursor = cursorNode.textValue();
                }

                harvestedJsons.add(json);
            } catch (Exception e) {
                retryCounter++;
                if (retryCounter > maxRetries) {
                    throw e;
                }
                LOG.warn("Processing URL: {} has failed. Retrying...", (target != null) ? (target.getUri() != null) ? target.getUri().toString() : null : null);
                LOG.warn("Reason: ", e);
                // sleep
                Thread.sleep(retryDelay * 1000);
            }
        }
        return harvestedJsons;
    }
}
