package sk.eea.td.hp_client.impl;

import org.glassfish.jersey.client.ClientConfig;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.api.License;
import sk.eea.td.hp_client.api.PinnerType;
import sk.eea.td.hp_client.util.ApiTokenFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.TreeMap;

public class HPClientImpl implements HPClient {

    private final Client client;

    private final String baseURL;

    private final String apiKey;

    private final String apiSecret;

    private final ApiTokenFactory apiTokenFactory;

    public HPClientImpl(String URL, String apiKey, String apiSecret) {
        this.baseURL = URL;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.apiTokenFactory = new ApiTokenFactory(apiSecret);
        ClientConfig clientConfig = new ClientConfig();
        this.client = ClientBuilder.newClient(clientConfig);
    }

    @Override
    public Response getPin(String pinID) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("pin").path("get.json").queryParam("id", pinID);
        return target.request().get();
    }

    @Override
    public Response getPins(String projectSlug) {
        WebTarget target = client.target(baseURL).path("en").path("api").path(projectSlug).path("pin").path("get_gallery.json").queryParam("limit", 10000);
        return  target.request().get();
    }

    @Override
    public Response getProjectDetail(String projectSlug) {
        WebTarget target = client.target(baseURL).path("en").path("api").path(projectSlug).path("projects").path("get.json");
        return target.request().get();
    }

    @Override
    public Response createProject(String title, String owner, String lat, String lng, String range) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("projects").path("save.json");

        Map<String, String> data = new TreeMap<>();
        data.put("title", title);

        data.put("owners[0][id]", owner);


        data.put("timemap[lat]", lat);
        data.put("timemap[lng]", lng);
        data.put("timemap[range]", range);
        data.put("timemap[zoom]", "0");

        data.put("new_project", "true");
        data.put("api_key", apiKey);
        data.put("api_path", "projects/save.json");

        data.put("api_token", apiTokenFactory.getApiToken(data));

        return target.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(new MultivaluedHashMap<>(data)));
    }

    @Override
    public Response createPin(String caption, Long projectId, String lat, String lng, String range, String date, License license, PinnerType pinnerType, String content) {
        WebTarget target = client.target(baseURL).path("en").path("api").path("pin").path("save.json");

        Map<String, String> data = new TreeMap<>();
        data.put("caption", caption);

        data.put("repinned_projects[0][id]", projectId.toString());

        data.put("timemap[lat]", lat);
        data.put("timemap[lng]", lng);
        data.put("timemap[range]", range);

        data.put("date", date);
        data.put("license",license.getKey());
        data.put("pinner_type", pinnerType.name().toLowerCase());
        data.put("display[content]", content);

        data.put("api_key", apiKey);
        data.put("api_path","pin/save.json");

        data.put("api_token", apiTokenFactory.getApiToken(data));

        return target.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(new MultivaluedHashMap<>(data)));
    }
}
