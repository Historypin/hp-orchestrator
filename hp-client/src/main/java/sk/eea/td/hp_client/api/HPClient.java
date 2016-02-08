package sk.eea.td.hp_client.api;

public interface HPClient {

	String getPin(String pinID);
	String getProject(String projectSlug);
	String getCollection(String collectionId);
}
