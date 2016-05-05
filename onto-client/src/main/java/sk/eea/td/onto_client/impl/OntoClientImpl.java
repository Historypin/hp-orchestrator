package sk.eea.td.onto_client.impl;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.onto_client.api.OntoClient;
import sk.eea.td.onto_client.dto.ExtractResponseDTO;

public class OntoClientImpl implements OntoClient {

    protected static Logger LOG = LoggerFactory.getLogger(OntoClientImpl.class);

    private final Client client;

    private final String baseURL;

    private final String wskey;

    private final ObjectMapper objectMapper;

    public OntoClientImpl(String baseURL, String wskey) {
        this.baseURL = baseURL;
        this.wskey = wskey;
        this.objectMapper = new ObjectMapper();
        ClientConfig clientConfig = new ClientConfig();
        this.client = ClientBuilder.newClient(clientConfig);
        /*this.client = ClientBuilder.newClient(clientConfig).register(JacksonObjectMapperProvider.class)
                .register(JacksonFeature.class).register(new LoggingFilter(LOG, true));*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String extractCoordinatesFromTextByRelevance(String text) throws IOException {
        WebTarget target = client.target(baseURL).path("ces-en").path("extract");
        Response response = target.request(MediaType.TEXT_PLAIN)
                .header("Accept", "application/vnd.ontotext.ces+json")
                .header("X-JwtToken", wskey)
                .post(Entity.text(text));

        JsonNode root = objectMapper.readTree(response.readEntity(InputStream.class));
        if (root.has("mentions")) {
            String mostRelevantLocation = null;
            Double maxRelevance = -1d;
            for (JsonNode item : root.get("mentions")) {
                JsonNode type = item.get("type");
                if (type != null && "Location".equals(type.textValue())) {
                    JsonNode features = item.get("features");
                    if (features != null && features.has("isTrusted")) {
                        if (features.has("relevanceScore") && features.has("inst") && Boolean.parseBoolean(features.get("isTrusted").asText())) {
                            Double relevance = features.get("relevanceScore").asDouble();
                            if (relevance > maxRelevance) {
                                maxRelevance = relevance;
                                mostRelevantLocation = features.get("inst").asText();
                            }
                        }
                    }
                }
            }

            if (isNotEmpty(mostRelevantLocation)) {
                target = client.target(baseURL).path("concepts-en").path("concept").queryParam("uri", mostRelevantLocation);
                response = target.request()
                        .header("X-JwtToken", wskey)
                        .get();
                root = objectMapper.readTree(response.readEntity(InputStream.class));
                JsonNode coordinateLocation = root.get("coordinate location");
                if (coordinateLocation != null) {
                    for(JsonNode node : coordinateLocation) {
                        node = node.get("hasValue");
                        if (node != null) {
                            node = node.get("label");
                            if (node != null) {
                                return node.asText();
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public String extract(String text, String uri) throws JsonParseException, IOException {
//        String uri = "http://mint-projects.image.ntua.gr/data/foodanddrink/EUFD105370";
        WebTarget target = client.target(baseURL).queryParam("uri", uri);

        Response resp = target.request(MediaType.TEXT_XML).post(Entity.text(text));
        String respString = resp.readEntity(String.class);
        return respString;
    }

    @Override
    public ExtractResponseDTO extract2Object(String text, String uri) throws JsonParseException, IOException {
//        String uri = "http://mint-projects.image.ntua.gr/data/foodanddrink/EUFD105370";
        WebTarget target = client.target(baseURL).queryParam("uri", uri);
        System.out.println(target.toString());

        Response resp = target.request(MediaType.TEXT_XML).post(Entity.text(text));
        System.out.println(resp.getStatus());
        String respString = resp.readEntity(String.class);

//        InputStream respIS = getClass().getResourceAsStream("/extract-response.json");

        List<ExtractResponseDTO> dtos = objectMapper.readValue(respString, new TypeReference<List<ExtractResponseDTO>>(){});
        return (dtos == null || dtos.isEmpty()) ? null : dtos.get(0);
    }
}
