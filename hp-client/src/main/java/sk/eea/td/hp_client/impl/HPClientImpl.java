package sk.eea.td.hp_client.impl;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.jodah.recurrent.Recurrent;
import net.jodah.recurrent.RetryPolicy;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.api.Pin;
import sk.eea.td.hp_client.api.PinnerType;
import sk.eea.td.hp_client.api.Project;
import sk.eea.td.hp_client.dto.ListingsResponseDTO;
import sk.eea.td.hp_client.dto.PlacesResponseDTO;
import sk.eea.td.hp_client.dto.SaveResponseDTO;
import sk.eea.td.hp_client.util.ApiTokenFactory;
import sk.eea.td.hp_client.util.JacksonObjectMapperProvider;

public class HPClientImpl implements HPClient {

    private final static Logger LOG = Logger.getLogger(HPClientImpl.class.getName());
    private static final String FAILED_TO_UPDATE_PIN_COMMENTS = "Failed to update pin comments for id: %s";
    private static final String FAILED_TO_UPDATE_PIN_TAGS = "Failed to update pin tags for id: %s";

    private final Client client;

    private final String baseURL;

    private final String apiKey;

    private final String apiSecret;

    private final ApiTokenFactory apiTokenFactory;

    private final ObjectMapper objectMapper;

    private final RetryPolicy retryPolicy;

    public HPClientImpl(String URL, String apiKey, String apiSecret) {
        this.baseURL = URL;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.apiTokenFactory = new ApiTokenFactory(apiSecret);
        this.objectMapper = new ObjectMapper();
        this.retryPolicy = new RetryPolicy()
                .retryOn(failure -> failure instanceof ProcessingException)
                .withBackoff(2, 30, TimeUnit.SECONDS)
                .withMaxRetries(3);
        ClientConfig clientConfig = new ClientConfig();
        this.client = ClientBuilder.newClient(clientConfig)
                .register(JacksonObjectMapperProvider.class)
                .register(JacksonFeature.class)
                .register(new LoggingFilter(LOG, true));
    }

    @Override
    public Response getPin(Long pinID) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("pin").path("get.json")
                .queryParam("id", pinID);
        return Recurrent.with(retryPolicy).get(() -> target.request().get());
    }

    @Override
    public Response getProjectSlug(String projectSlug, long page) {
        WebTarget target = client.target(baseURL).path("en").path("api").path(projectSlug).path("pin")
                .path("get_gallery.json").queryParam("limit", 10000);
        if (page > 1)
            target = target.queryParam("page", page);
        return target.request().get();
    }

    @Override
    public Response getProjectDetail(String projectSlug) {
        WebTarget target = client.target(baseURL).path("en").path("api").path(projectSlug).path("projects")
                .path("get.json");
        return Recurrent.with(retryPolicy).get(() -> target.request().get());
    }

    @Override
    public SaveResponseDTO createProject(Long userId, Project project) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("projects").path("save.json");

        Map<String, String> data = new HashMap<>();
        data.put("title", project.getTitle());

        data.put("owners[0][id]", userId.toString());

        data.put("timemap[lat]", project.getLocation().getLat().toString());
        data.put("timemap[lng]", project.getLocation().getLng().toString());
        data.put("timemap[range]", project.getLocation().getRange().toString());
        data.put("timemap[zoom]",
                "0"); // TODO: temporary workaround, we need to provide default value, otherwise the project will not be valid

        data.put("new_project", "true");
        data.put("api_key", apiKey);
        data.put("api_path", "projects/save.json");

        data.put("api_token", apiTokenFactory.getApiToken(data));

        return Recurrent.with(retryPolicy)
                .get(() ->
                        target.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(new MultivaluedHashMap<>(data)))
                                .readEntity(SaveResponseDTO.class)
                );
    }

    @Override
    public SaveResponseDTO createPin(Long projectId, Pin pin) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("pin").path("save.json");

        Map<String, String> data = new HashMap<>();
        data.put("caption", pin.getCaption());
        data.put("description", pin.getDescription());

        data.put("repinned_projects[0][id]", projectId.toString());

        data.put("location[lat]", pin.getLocation().getLat().toString());
        data.put("location[lng]", pin.getLocation().getLng().toString());
        data.put("location[range]", pin.getLocation().getRange().toString());

        data.put("remote[id]", pin.getRemoteId());
        data.put("remote[provider_id]", pin.getRemoteProviderId());

        data.put("date", pin.getDate());
        data.put("license", pin.getLicense());
        data.put("link", pin.getLink());

        data.put("pinner_type", pin.getPinnerType().name().toLowerCase());
        if (PinnerType.PHOTO.equals(pin.getPinnerType())) {
            data.put("image_url", pin.getContent());
        }
        data.put("display[content]", pin.getContent());

        if (isNotEmpty(pin.getTags())) {
            String[] tags = pin.getTags().split(",");
            for (int i = 0; i < tags.length; i++) {
                data.put(String.format("tags[%d][text]", i), tags[i].trim());
            }
        }

        data.put("api_key", apiKey);
        data.put("api_path", "pin/save.json");

        data.put("api_token", apiTokenFactory.getApiToken(data));

        return Recurrent.with(retryPolicy).get(() ->
                target.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(new MultivaluedHashMap<>(data)))
                        .readEntity(SaveResponseDTO.class)
        );
    }

    @Override
    public Response deleteProject(Long projectId) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("projects").path("delete.json")
                .queryParam("id", projectId);

        Map<String, String> data = new HashMap<>();
        data.put("api_key", apiKey);
        data.put("api_path", "projects/delete.json");
        data.put("id", projectId.toString());

        data.put("api_token", apiTokenFactory.getApiToken(data));

        return Recurrent.with(retryPolicy).get(() ->
                target.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(new MultivaluedHashMap<>(data)))
        );
    }

    @Override
    public Response deletePin(Long pinId) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("pin").path("delete.json")
                .queryParam("id", pinId);

        Map<String, String> data = new HashMap<>();
        data.put("api_key", apiKey);
        data.put("api_path", "pin/delete.json");
        data.put("id", pinId.toString());

        data.put("api_token", apiTokenFactory.getApiToken(data));

        return Recurrent.with(retryPolicy).get(() ->
                target.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(new MultivaluedHashMap<>(data)))
        );
    }

    @Override
    public void deleteAllPins(Long user) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("pin").path("listing.json")
                .queryParam("user", user).queryParam("limit", 1000000);
        ListingsResponseDTO response = Recurrent.with(retryPolicy).get(() ->
                target.request().get().readEntity(ListingsResponseDTO.class)
        );
        response.getResults().forEach(r -> deletePin(r.getId()));
    }

    @Override
    public void deleteAllProjects(Long user) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("projects").path("listing.json")
                .queryParam("user", user).queryParam("limit", 1000000);
        ListingsResponseDTO response = Recurrent.with(retryPolicy).get(() ->
                target.request().get().readEntity(ListingsResponseDTO.class)
        );
        response.getResults().forEach(r -> deleteProject(r.getId()));
    }

    @Override
    public PlacesResponseDTO getPlaces(String countrySlug) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("places").path("get.json")
                .queryParam("places", countrySlug);
        return Recurrent.with(retryPolicy).get(() ->
                target.request().get().readEntity(PlacesResponseDTO.class)
        );
    }

    @Override
    public List<String> updatePin(Long id, List<String> tags, List<String> places) {

        String sTags = StringUtils.join(tags, ',');
        String sPlaces = StringUtils.join(places, ',');
        String l = String.format("HPClient: Update pin id: %s, tags: %s, places: %s", id, sTags, sPlaces);
        LOG.log(Level.FINE, l);

        List<String> errors = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(tags)) {

            WebTarget pinTarget = client.target(baseURL).path("en").path("api").path("pin").path("save.json");
            Map<String, String> pinData = new HashMap<>();
            pinData.put("id", String.valueOf(id));

            for (int i = 0; i < tags.size(); i++) {
                pinData.put(String.format("tags[%d][text]", i), tags.get(i));
            }
            pinData.put("api_key", apiKey);
            pinData.put("api_path", "pin/save.json");
            pinData.put("api_token", apiTokenFactory.getApiToken(pinData));

            Response r = Recurrent.with(retryPolicy).get(() ->
                pinTarget.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(new MultivaluedHashMap<>(pinData))));
            if (r.getStatus() != Response.Status.OK.getStatusCode()) {
                errors.add(String.format(FAILED_TO_UPDATE_PIN_TAGS, id));
            }
        }

        if (CollectionUtils.isNotEmpty(places)) {

            WebTarget commentTarget = client.target(baseURL).path("en").path("api").path("comments").path("post.json");
            Map<String, String> commentData = new HashMap<>();
            commentData.put("item_id", String.valueOf(id));
            commentData.put("copy", StringUtils.join(places, ','));
            commentData.put("api_key", apiKey);
            commentData.put("api_path", "comments/post.json");
            commentData.put("api_token", apiTokenFactory.getApiToken(commentData));

            Response r = Recurrent.with(retryPolicy).get(() ->
                commentTarget.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(new MultivaluedHashMap<>(commentData))));
            if (r.getStatus() != Response.Status.OK.getStatusCode()) {
                errors.add(String.format(FAILED_TO_UPDATE_PIN_COMMENTS, id));
            }
        }

        return errors;
    }

    @Override
    public Response getAnnotations(String from, String until) throws IllegalArgumentException {
        if (from == null || until == null)
            throw new IllegalArgumentException("'from' and 'until' have to be set in order to harvest annotations");
        WebTarget target = client.target(baseURL).path("api").path("services/").path("annotations")
                .queryParam("from", from).queryParam("until", until);
        return Recurrent.with(retryPolicy).get(() -> target.request().get());
    }
}
