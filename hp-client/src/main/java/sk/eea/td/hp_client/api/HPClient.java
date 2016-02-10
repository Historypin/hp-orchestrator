package sk.eea.td.hp_client.api;

import javax.ws.rs.core.Response;

public interface HPClient {

	Response getPin(String pinID);
	Response getPins(String projectSlug);
	Response getProjectDetail(String projectSlug);
	Response createProject(String title, String owner, String lat, String lng, String range);
	Response createPin(String caption, Long projectId, String lat, String lng, String range, String date, License license, PinnerType pinnerType, String content);
}
