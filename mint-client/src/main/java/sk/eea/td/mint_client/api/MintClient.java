package sk.eea.td.mint_client.api;

import java.io.File;

import org.json.JSONObject;

public interface MintClient {

	String HP_MAPPING = "1157";

	/**
	 * Login user into MINT.
	 * @param username MINT username
	 * @param password MINT password
	 * @return
	 */
	boolean login(String username, String password);

	// return the datasetId or null if there is none
	Integer uploadJson(File f);

	// Use this to get all the status of the dataset
	JSONObject completeStatus(int id);

	// return the current status of the set
	// running, ready, error, ? 
	// loading, loading_failed, 
	String findStatus(int datasetId);

	// transform according to histroypin mapping
	boolean transform(int datasetId);

	// when set is imported, send the correct define items
	boolean defineItems(int datasetId);

	// try to publish the given id
	// has to be transformed status should reflect that
	// http://mint-projects.image.ntua.gr/foodanddrink/XSLselection?uploadId=1960&orgId=1032&userId=-1
	boolean publish(int datasetId);

	//Wait for id dataset to become ready (not running) but only timeOutSeconds
	// return true on ready, false on timeout or error
	boolean waitForReady(int id, int timeOutSeconds);

}