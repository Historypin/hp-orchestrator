package sk.eea.td.hp_client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.api.PinnerType;
import sk.eea.td.hp_client.dto.ListingsResponseDTO;
import sk.eea.td.hp_client.dto.SaveResponseDTO;
import sk.eea.td.hp_client.util.ApiTokenFactory;
import sk.eea.td.hp_client.util.JacksonObjectMapperProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class HPClientImpl implements HPClient {

    private final static Logger LOG = Logger.getLogger(HPClientImpl.class.getName());

    private final Client client;

    private final String baseURL;

    private final String apiKey;

    private final String apiSecret;

    private final ApiTokenFactory apiTokenFactory;

    private final ObjectMapper objectMapper;

    public HPClientImpl(String URL, String apiKey, String apiSecret) {
        this.baseURL = URL;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.apiTokenFactory = new ApiTokenFactory(apiSecret);
        this.objectMapper = new ObjectMapper();
        ClientConfig clientConfig = new ClientConfig();
        this.client = ClientBuilder.newClient(clientConfig)
                .register(JacksonObjectMapperProvider.class)
                .register(JacksonFeature.class)
                .register(new LoggingFilter(LOG, true));
    }

    @Override
    public Response getPin(Long pinID) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("pin").path("get.json").queryParam("id", pinID);
        return target.request().get();
    }

    @Override
    public Response getPins(String projectSlug) {
        WebTarget target = client.target(baseURL).path("en").path("api").path(projectSlug).path("pin").path("get_gallery.json").queryParam("limit", 10000);
        return target.request().get();
    }

    @Override
    public Response getProjectDetail(String projectSlug) {
        WebTarget target = client.target(baseURL).path("en").path("api").path(projectSlug).path("projects").path("get.json");
        return target.request().get();
    }

    @Override
    public SaveResponseDTO createProject(String title, Long user, String lat, String lng, String range) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("projects").path("save.json");

        Map<String, String> data = new HashMap<>();
        data.put("title", title);

        data.put("owners[0][id]", user.toString());

        data.put("timemap[lat]", lat);
        data.put("timemap[lng]", lng);
        data.put("timemap[range]", range);
        data.put("timemap[zoom]", "0"); // TODO: temporary workaround, we need to provide default value, otherwise the project will not be valid

        data.put("new_project", "true");
        data.put("api_key", apiKey);
        data.put("api_path", "projects/save.json");

        data.put("api_token", apiTokenFactory.getApiToken(data));

        return target.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(new MultivaluedHashMap<>(data))).readEntity(SaveResponseDTO.class);
    }

    @Override
    public SaveResponseDTO createPin(String caption, Long projectId, String lat, String lng, String range, String date, String license, PinnerType pinnerType, String content) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("pin").path("save.json");

        Map<String, String> data = new HashMap<>();
        data.put("caption", caption);

        data.put("repinned_projects[0][id]", projectId.toString());

        data.put("timemap[lat]", lat);
        data.put("timemap[lng]", lng);
        data.put("timemap[range]", range);

        data.put("date", date);
        data.put("license", license);

        data.put("pinner_type", pinnerType.name().toLowerCase());
        if (PinnerType.PHOTO.equals(pinnerType)) {
            data.put("image_url", content);
        }
        data.put("display[content]", content);

        data.put("api_key", apiKey);
        data.put("api_path", "pin/save.json");

        data.put("api_token", apiTokenFactory.getApiToken(data));

        return target.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(new MultivaluedHashMap<>(data))).readEntity(SaveResponseDTO.class);
    }

    @Override
    public SaveResponseDTO createPin(String caption, String description, Long projectId, String rawLocation, String date, String license, PinnerType pinnerType, String content) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("pin").path("save.json");

        Map<String, String> data = new HashMap<>();
        data.put("caption", caption);
        data.put("description", description);

        data.put("repinned_projects[0][id]", projectId.toString());

        data.put("location[raw]", rawLocation);

        data.put("date", date);
        data.put("license", license);

        data.put("pinner_type", pinnerType.name().toLowerCase());
        if (PinnerType.PHOTO.equals(pinnerType)) {
            data.put("image_url", content);
        }
        data.put("display[content]", content);

        data.put("api_key", apiKey);
        data.put("api_path", "pin/save.json");

        data.put("api_token", apiTokenFactory.getApiToken(data));

        return target.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(new MultivaluedHashMap<>(data))).readEntity(SaveResponseDTO.class);
    }

    @Override
    public Response deleteProject(Long projectId) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("projects").path("delete.json").queryParam("id", projectId);

        Map<String, String> data = new HashMap<>();
        data.put("api_key", apiKey);
        data.put("api_path", "projects/delete.json");
        data.put("id", projectId.toString());

        data.put("api_token", apiTokenFactory.getApiToken(data));

        return target.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(new MultivaluedHashMap<>(data)));
    }

    @Override
    public Response deletePin(Long pinId) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("pin").path("delete.json").queryParam("id", pinId);

        Map<String, String> data = new HashMap<>();
        data.put("api_key", apiKey);
        data.put("api_path", "pin/delete.json");
        data.put("id", pinId.toString());

        data.put("api_token", apiTokenFactory.getApiToken(data));

        return target.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(new MultivaluedHashMap<>(data)));
    }

    @Override
    public void deleteAllPins(Long user) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("pin").path("listing.json").queryParam("user", user).queryParam("limit", 1000000);
        ListingsResponseDTO response = target.request().get().readEntity(ListingsResponseDTO.class);
        response.getResults().forEach(r -> deletePin(r.getId()));
    }

    @Override
    public void deleteAllProjects(Long user) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("projects").path("listing.json").queryParam("user", user).queryParam("limit", 1000000);
        ListingsResponseDTO response = target.request().get().readEntity(ListingsResponseDTO.class);
        response.getResults().forEach(r -> deleteProject(r.getId()));
    }
}
