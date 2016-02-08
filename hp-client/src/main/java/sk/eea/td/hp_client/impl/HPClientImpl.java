package sk.eea.td.hp_client.impl;

import org.glassfish.jersey.client.ClientConfig;
import sk.eea.td.hp_client.api.HPClient;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class HPClientImpl implements HPClient {

	private Client client;

	private String baseURL;

	public HPClientImpl(String URL) {
		this.baseURL = URL;
		ClientConfig clientConfig = new ClientConfig();
		this.client = ClientBuilder.newClient(clientConfig);
	}

	@Override
	public String getPin(String pinID) {
		WebTarget target = client.target(baseURL).path("en").path("api").path("pin").path("get.json").queryParam("id", pinID);
		Response response = target.request().get();
		return response.readEntity(String.class);
	}

	@Override
	public String getPins(String projectSlug) {
		WebTarget target = client.target(baseURL).path("en").path("api").path(projectSlug).path("pin").path("get_gallery.json").queryParam("limit", 10000);
		Response response = target.request().get();
		return response.readEntity(String.class);
	}

	@Override
	public String getProjectDetail(String projectSlug) {
		WebTarget target = client.target(baseURL).path("en").path("api").path(projectSlug).path("projects").path("get.json");
		Response response = target.request().get();
		return response.readEntity(String.class);
	}
}
