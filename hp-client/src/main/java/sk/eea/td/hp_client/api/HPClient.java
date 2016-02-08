package sk.eea.td.hp_client.api;

public interface HPClient {

	String getPin(String pinID);
	String getPins(String projectSlug);
	String getProjectDetail(String projectSlug);
}
