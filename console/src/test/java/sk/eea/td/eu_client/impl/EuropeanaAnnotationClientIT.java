package sk.eea.td.eu_client.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import sk.eea.td.IntegrationTest;
import sk.eea.td.eu_client.api.EuropeanaAnnotationClient;

@Category(IntegrationTest.class)
public class EuropeanaAnnotationClientIT {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCreateAnnotation() {
		String baseUrl = "http://test-annotations.europeana.eu/";
		String apiKey = "apidemo";
		String user = "tester1";
		EuropeanaAnnotationClient client = new EuropeanaAnnotationClientImpl(baseUrl, apiKey, user);

		StringBuffer annotationJson = new StringBuffer();
		
		String responseJson;
		try {
			InputStream is = ClassLoader.getSystemResourceAsStream("EU_annotation.json");
			byte[] charBuffer = new byte[100];
			while(is.read(charBuffer)> 0){
				annotationJson.append(new String(charBuffer, "UTF-8"));
			}
			responseJson = client.createAnnotation(annotationJson.toString());
			JSONObject object = new JSONObject(responseJson);
			String id = object.getString("@id");
			assertNotNull(id);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	@Test
	public void fail1CreateAnnotation() {
		StringBuffer annotationJson = new StringBuffer();
		String baseUrl = "http://test-annotations.europeana.eu/";
		String apiKey = "";
		String user = "";
		EuropeanaAnnotationClient client = new EuropeanaAnnotationClientImpl(baseUrl, apiKey, user);
		
		try {
			InputStream is = ClassLoader.getSystemResourceAsStream("EU_annotation.json");
			byte[] charBuffer = new byte[100];
			while(is.read(charBuffer)> 0){
				annotationJson.append(charBuffer);
			}
			@SuppressWarnings("unused")
            String responseJson = client.createAnnotation(annotationJson.toString());
			fail();
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("User not logged in"));			
		}
	}

	@Test
	public void fail2CreateAnnotation() {
		String baseUrl = "http://test-annotations.europeana.eu/";
		String apiKey = "apidemo";
		String user = "anonymous";
		EuropeanaAnnotationClient client = new EuropeanaAnnotationClientImpl(baseUrl, apiKey, user);

		StringBuffer annotationJson = new StringBuffer();
		
		String responseJson;
		try {
			InputStream is = ClassLoader.getSystemResourceAsStream("EU_annotation.json");
			byte[] charBuffer = new byte[100];
			while(is.read(charBuffer)> 0){
				annotationJson.append(charBuffer);
			}
			responseJson = client.createAnnotation(annotationJson.toString());
            @SuppressWarnings("unused")
			JSONObject object = new JSONObject(responseJson);
			fail();
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("User not authorized"));	
		}
	}

}
