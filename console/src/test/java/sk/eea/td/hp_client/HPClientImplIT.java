package sk.eea.td.hp_client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sk.eea.td.IntegrationTest;
import sk.eea.td.config.IntegrationTestConfig;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.api.Location;
import sk.eea.td.hp_client.api.Pin;
import sk.eea.td.hp_client.api.PinnerType;
import sk.eea.td.hp_client.api.Project;
import sk.eea.td.hp_client.dto.PlacesResponseDTO;
import sk.eea.td.hp_client.dto.SaveResponseDTO;
import sk.eea.td.hp_client.impl.HPClientImpl;

/**
 * Integration test for HPClientImpl class.
 * Integration test consists of basic CRUD operations. Because of that we need to assure the execution order of the test methods.
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationTestConfig.class)
@PropertySource({ "classpath:default.properties", "classpath:integration.properties"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HPClientImplIT {

    @Value("${historypin.base.url}")
    private String baseURL;

    @Value("${historypin.api.key}")
    private String apiKey;

    @Value("${historypin.api.secret}")
    private String apiSecret;

    @Value("${historypin.user}")
    private Long user;

    private static final String PROJECT_NAME = "My test collection";

    private static final String PIN_NAME = "My test pin";

    private static final String PIN_DESCRIPTION = "This is accurate and complete description of this pin";

    private static final String URL = "http://example.com";

    private static final String TAGS = "much wow, very pin, so tags";

    private static final String REMOTE_ID = "/this/is_unique00000/id";

    private static final String REMOTE_PROVIDER_ID = "1";

    private static final Location LOCATION = new Location(42.0, 23.0, 2000L);

    private static String createdProjectSlug;

    private static Long createdProjectId;

    private static List<Long> createdPinsIds = new ArrayList<>();

    private HPClient client;

    private JSONParser jsonParser;

    @Before
    public void init() {
        this.client = new HPClientImpl(baseURL, apiKey, apiSecret);
        this.jsonParser = new JSONParser();
    }

    /* CREATE */
    @Test
    public void test_AA_CreateProject() throws Exception {
        SaveResponseDTO response = client.createProject(user, new Project(PROJECT_NAME, LOCATION));

        assertThat(response.isSuccess(), is(equalTo(true)));
        assertThat(response.getSlug(), is(not(isEmptyString())));
        assertThat(response.getId(), is(not(equalTo(0))));
        createdProjectSlug = response.getSlug();
        createdProjectId = response.getId();
    }

    @Test
    public void test_A_CreatePin() throws Exception {
        assertThat(createdProjectId, is(notNullValue()));

        LocalDate now = LocalDate.now();
        String date = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
        Pin pin = new Pin(
                PinnerType.TEXT,
                PIN_NAME,
                PIN_DESCRIPTION,
                date,
                "no-copyright",
                LOCATION,
                URL,
                "This is test pin content",
                TAGS,
                REMOTE_ID,
                REMOTE_PROVIDER_ID
        );
        SaveResponseDTO response = client.createPin(createdProjectId, pin);

        assertThat(response.isSuccess(), is(equalTo(true)));
        assertThat(response.getId(), is(not(equalTo(0))));

        createdPinsIds.add(response.getId());
        assertThat(createdPinsIds, is(not(empty())));
    }

    @Test
    public void test_A_CreateSamplePins() throws Exception {
        assertThat(createdProjectId, is(notNullValue()));
        LocalDate now = LocalDate.now();
        String date = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
        for (int i = 0; i < 3; i++) {
            Pin pin = new Pin(
                    PinnerType.TEXT,
                    PIN_NAME,
                    PIN_DESCRIPTION,
                    date,
                    "no-copyright",
                    LOCATION,
                    URL,
                    "This is test pin content",
                    TAGS,
                    REMOTE_ID,
                    REMOTE_PROVIDER_ID
            );
            SaveResponseDTO response = client.createPin(createdProjectId, pin);
            assertThat(response.isSuccess(), is(equalTo(true)));
            assertThat(response.getId(), is(not(equalTo(0))));

            createdPinsIds.add(response.getId());
        }
    }

    /* READ */
    @Test
    public void test_B_GetPin() throws Exception {
        assertThat(createdPinsIds, is(not(empty())));
        Response response = client.getPin(createdPinsIds.get(0));
//        Response response = client.getPin(89832l);
        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String responseMessage = response.readEntity(String.class);
        JSONObject jsonObject = (JSONObject) jsonParser.parse(responseMessage);

//		Writer writer = new FileWriter("/tmp/89832.json");
//		jsonObject.writeJSONString(writer);
//		writer.close();
        
        String caption = (String) jsonObject.get("caption");
        Long user_id = (Long) jsonObject.get("user_id");
        assertThat(caption, is(equalTo(PIN_NAME)));
        assertThat(user_id, is(equalTo(user)));
    }

    @Test
    public void test_B_GetPins() throws Exception {
        assertThat(createdProjectSlug, is(not(isEmptyString())));
        Response response = client.getProjectSlug(createdProjectSlug, 1);
//        Response response = client.getProjectSlug("new-orleans", 1);
        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        String responseMessage = response.readEntity(String.class);
        JSONObject jsonObject = (JSONObject) jsonParser.parse(responseMessage);
        JSONArray jsonArray = (JSONArray) jsonObject.get("results");

//        Writer writer = new FileWriter("/tmp/new-orleans.json");
//        jsonObject.writeJSONString(writer);
//        writer.close();
        
        for (Object object : jsonArray) {
            JSONObject pin = (JSONObject) object;
            Long id = (Long) pin.get("id");
            assertThat(id, is(notNullValue()));
            assertThat(createdPinsIds, hasItem(id));
        }
    }

    @Test
    public void test_B_GetProjectDetail() throws Exception {
        assertThat(createdProjectSlug, is(not(isEmptyString())));
        Response response = client.getProjectDetail(createdProjectSlug);
        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String responseMessage = response.readEntity(String.class);
        JSONObject jsonObject = (JSONObject) jsonParser.parse(responseMessage);
        String projectName = (String) jsonObject.get("title");
        String ownerId = (String) jsonObject.get("owner_id");
        assertThat(projectName, is(equalTo(PROJECT_NAME)));
        assertThat(ownerId, is(equalTo(user.toString())));
    }

    @Test
    public void test_B_GetPlaces() throws Exception {
        PlacesResponseDTO response = client.getPlaces("bulgaria");
        assertThat(response.getId(), is(notNullValue()));
        assertThat(response.getBounds(), is(notNullValue()));
        assertThat(response.getBounds().getNe(), is(notNullValue()));
        assertThat(response.getBounds().getSw(), is(notNullValue()));
    }

    /* UPDATE */

    /* DELETE */
    @Test
    public void test_D_DeleteProject() throws Exception {
        assertThat(createdProjectId, is(notNullValue()));
        Response response = client.deleteProject(createdProjectId);

        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String responseMessage = response.readEntity(String.class);
        JSONObject jsonObject = (JSONObject) jsonParser.parse(responseMessage);
        String status = (String) jsonObject.get("Status");
        assertThat(status, is(equalTo("Done")));
    }

    @Test
    public void test_D_DeletePin() throws Exception {
        assertThat(createdPinsIds, is(not(empty())));
        for (Long pinId : createdPinsIds) {
            Response response = client.deletePin(pinId);
            assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

            String responseMessage = response.readEntity(String.class);
            JSONObject jsonObject = (JSONObject) jsonParser.parse(responseMessage);
            String status = (String) jsonObject.get("Status");
            assertThat(status, is(equalTo("Done")));
        }
    }
    
    @Test
    public void test_G_Annotations(){
    	Response response = client.getAnnotations("2010-03-15T13:45:30Z", "2016-05-15T13:45:30Z");
    	String entity = response.readEntity(String.class);
		org.json.JSONObject object = new org.json.JSONObject(entity);
    	assertEquals(object.get("Status"),"ok");
    	assertTrue(object.has("items"));
    	assertNotNull(object.getJSONArray("items"));
    }
}
