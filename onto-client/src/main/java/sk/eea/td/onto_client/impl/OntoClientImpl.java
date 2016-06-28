package sk.eea.td.onto_client.impl;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.jsonld.JSONLDParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.onto_client.api.OntoClient;
import sk.eea.td.onto_client.dto.EnrichResponseDTO;
import sk.eea.td.onto_client.dto.EnrichmentDTO;

public class OntoClientImpl implements OntoClient {

    private static final Logger LOG = LoggerFactory.getLogger(OntoClientImpl.class);

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

        WebTarget target = client.target(baseURL).queryParam("uri", uri);
        Response resp = target.request(MediaType.TEXT_XML).post(Entity.text(text));
        String respString = resp.readEntity(String.class);
        return respString;
    }

    @Override
    public EnrichResponseDTO extract2Object(String text, String uri) throws JsonParseException, IOException {

        WebTarget target = client.target(baseURL).queryParam("uri", uri);
        Response resp = target.request(MediaType.TEXT_XML).post(Entity.text(text));

        List<EnrichResponseDTO> dtos = null;

        if (resp != null && Response.Status.OK.getStatusCode() == resp.getStatus()) {
            String respString = resp.readEntity(String.class);
            LOG.debug(respString);
            dtos = objectMapper.readValue(respString, new TypeReference<List<EnrichResponseDTO>>(){});
        } else {
            LOG.error("Ontotext client request to {} failed with status {}", baseURL, resp == null ? null : resp.getStatus());
        }

        EnrichResponseDTO selected = null;
        try {
            selected = selectOne(dtos, uri);
        } catch (Exception e) {
            //failed to find one with the given url
            LOG.warn("Ontotext client received an invalid response");
        }
        return selected;
    }

    @Override
    public EnrichmentDTO extractUsingJsonLDParser(String text, String uri) throws RDFParseException, RDFHandlerException, IOException {

        EnrichmentDTO result = null;
        WebTarget target = client.target(baseURL).queryParam("uri", uri);
        Response resp = target.request(MediaType.TEXT_XML).post(Entity.text(text));
        if (resp != null && Response.Status.OK.getStatusCode() == resp.getStatus()) {
            InputStream respIS = resp.readEntity(InputStream.class);

            //RDFParser parser = new SesameJSONLDParser();
            RDFParser parser = new JSONLDParser();
            StatementCollector collector = new StatementCollector(new LinkedList<>());
            parser.setRDFHandler(collector);
            parser.parse(respIS, "http://efd.ontotext.com/context/efd-context.jsonld");
            Collection<Statement> statements = collector.getStatements();
            
            Collection<Statement> tags = findTags(statements);
            Collection<Statement> places = findPlaces(statements);

            List<String> t = tags.stream().map(tag -> tag.getObject().stringValue()).collect(Collectors.toList());
            List<String> p = places.stream().map(place -> place.getObject().stringValue()).collect(Collectors.toList());

            result = new EnrichmentDTO();
            result.setPlaces(p);
            result.setTags(t);
        }

        return result;
    }

    private static Collection<Statement> findTags(Collection<Statement> statements) {
        return statements.stream().filter(s -> "dct:subject".equalsIgnoreCase(s.getPredicate().toString())).collect(Collectors.toList());
    }
    private static Collection<Statement> findPlaces(Collection<Statement> statements) {
        return statements.stream().filter(s -> "dct:spatial".equalsIgnoreCase(s.getPredicate().toString())).collect(Collectors.toList());
    }

    private EnrichResponseDTO selectOne(List<EnrichResponseDTO> dtos, String uri) {
        if (dtos == null || dtos.isEmpty()) {
            return null;
        }
        return dtos.stream().filter(dto -> uri.equalsIgnoreCase(dto.getId())).findFirst().get();
    }
}
