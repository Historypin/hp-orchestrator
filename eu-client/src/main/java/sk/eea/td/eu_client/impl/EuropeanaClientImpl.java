package sk.eea.td.eu_client.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jodah.recurrent.Recurrent;
import net.jodah.recurrent.RetryPolicy;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.eea.td.eu_client.api.EuropeanaClient;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class EuropeanaClientImpl implements EuropeanaClient {

    protected static Logger LOG = LoggerFactory.getLogger(EuropeanaClientImpl.class);

    private final Client client;

    private final String baseURL;

    private final String wskey;
    
    private final ObjectMapper objectMapper;

    private final JsonFactory jsonFactory;

    private final RetryPolicy retryPolicy;

    private static final String EDM_IS_SHOWN_BY = "edmIsShownBy";

    public EuropeanaClientImpl(String baseURL, String wskey) {
        this.baseURL = baseURL;
        this.wskey = wskey;
        this.jsonFactory = new JsonFactory();
        this.objectMapper = new ObjectMapper(this.jsonFactory);
        this.retryPolicy = new RetryPolicy()
                .retryOn(failure -> failure instanceof ProcessingException)
                .withBackoff(2, 30, TimeUnit.SECONDS)
                .withMaxRetries(3);
        ClientConfig clientConfig = new ClientConfig();
        this.client = ClientBuilder.newClient(clientConfig);
    }

    @Override
    public String getRecord(String id) throws IOException, InterruptedException {
        final WebTarget target = client.target(baseURL).path("api").path("v2").path("record").path(id.concat(".json"))
                .queryParam("wskey", wskey);
        final Response response = Recurrent.with(retryPolicy).get(() ->
                target.request().get()
        );
        return response.readEntity(String.class);
    }

    /**
     * Get value of field 'edmIsShownBy' from Europeana record by given item ID.
     * Uses Jackson Streaming api to get the field's value.
     * Return null if field is missing.
     *
     * @param id Europeana item ID.
     * @return Value of 'edmIsShownBy' field of null if not found.
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public String getRecordsEdmIsShownBy(String id) throws IOException, InterruptedException {
        final String response = getRecord(id);
        final JsonParser jsonParser = this.jsonFactory.createParser(response);
        while (!jsonParser.isClosed()) {
            final JsonToken jsonToken = jsonParser.nextToken();
            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                final String fieldname = jsonParser.getCurrentName();
                if (EDM_IS_SHOWN_BY.equals(fieldname)) {
                    //move to next token
                    jsonParser.nextToken();
                    return jsonParser.getValueAsString();
                }
            }
        }
        return null;
    }

    @Override
    public List<String> search(String luceneQuery) throws IOException, InterruptedException {
        return this.search(luceneQuery, null);
    }

    @Override 
    public List<String> search(String luceneQuery, String facet) throws IOException, InterruptedException {
        String cursor = "*"; // initial cursor value
        List<String> harvestedJsons = new ArrayList<>();
        while (!"".equals(cursor)) {
            final WebTarget target = client.target(baseURL).path("api").path("v2").path("search.json")
                    .queryParam("wskey", wskey)
                    .queryParam("profile", "rich")
                    .queryParam("query", luceneQuery)
                    .queryParam("cursor", cursor);

            if (isNotEmpty(facet)) {
                target.queryParam("qf", facet);
            }

            Response response = Recurrent.with(retryPolicy).get(() ->
                    target.request().get()
            );
            String json = response.readEntity(String.class);

            JsonNode rootNode = objectMapper.readTree(json);

            // usage of cursor should erase it
            cursor = "";

            JsonNode cursorNode = rootNode.get("nextCursor");
            if (cursorNode != null) {
                cursor = cursorNode.textValue();
            }

            harvestedJsons.add(json);
        }
        return harvestedJsons;
    }
}
