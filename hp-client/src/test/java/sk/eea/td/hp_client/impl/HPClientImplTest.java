package sk.eea.td.hp_client.impl;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.api.License;
import sk.eea.td.hp_client.api.PinnerType;

import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;

/**
 * Integration test for HPClientImpl class.
 * Integration test consists of basic CRUD operations. Because of that we need to assure the execution order of the test methods.
 */
@Category(IntegrationTest.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HPClientImplTest {

    private static final String URL = "http://v76-beta-1.historypin-hrd.appspot.com";

    private static final String API_KEY = "d82e14f622f1fbd475da1ff2fba8b118f4756f368fd7f7a5a30f8beb705d743a";

    private static final String API_SECRET = "c61b9a9ad72b02264ac1997165309316";

    private static final String PROJECT_NAME = "My test collection";

    private static final String PIN_NAME = "My test pin";

    private static final String USER_ID = "65543";

    private static String projectSlug;

    private static Long projectId;

    private static String pinId;

    private HPClient client;

    private JSONParser jsonParser;

    @Before
    public void init() {
        this.client = new HPClientImpl(URL, API_KEY, API_SECRET);
        this.jsonParser = new JSONParser();
    }

    /* CREATE */
    @Test
    public void test_AA_CreateProject() throws Exception {
        Response response = client.createProject(PROJECT_NAME, USER_ID, "42", "23", "2000");
        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String responseMessage = response.readEntity(String.class);
        JSONObject jsonObject = (JSONObject) jsonParser.parse(responseMessage);
        projectSlug = (String) jsonObject.get("slug");
        projectSlug = projectSlug.replace("/", "");
        boolean status = (boolean) jsonObject.get("success");
        assertThat(status, is(equalTo(true)));
        assertThat(projectSlug, is(not(isEmptyString())));

        // TODO: remove this, after HP API will return project ID after projects creation
        // This is just temporal workaround
        response = client.getProjectDetail(projectSlug);
        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        responseMessage = response.readEntity(String.class);
        jsonObject = (JSONObject) jsonParser.parse(responseMessage);

        projectId = (Long) jsonObject.get("id");
        assertThat(projectId, is(notNullValue()));
    }

    @Test
    public void test_A_CreatePin() throws Exception {
        LocalDate now = LocalDate.now();
        String date = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
        Response response = client.createPin(
                PIN_NAME,
                projectId,
                "42", "23", "2000",
                date,
                License.NO_COPYRIGHT,
                PinnerType.TEXT,
                "This is test pin content"
        );
        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String responseMessage = response.readEntity(String.class);
        JSONObject jsonObject = (JSONObject) jsonParser.parse(responseMessage);
        boolean status = (boolean) jsonObject.get("success");
        pinId = (String) jsonObject.get("id");
        assertThat(status, is(equalTo(true)));
        assertThat(pinId, is(not(isEmptyString())));
    }

    /* READ */
    @Test
    public void test_B_GetPin() throws Exception {
        Response response = client.getPin(pinId);
        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String responseMessage = response.readEntity(String.class);
        JSONObject jsonObject = (JSONObject) jsonParser.parse(responseMessage);
        String caption = (String) jsonObject.get("caption");
        Long user_id = (Long) jsonObject.get("user_id");
        assertThat(caption, is(equalTo(PIN_NAME)));
        assertThat(user_id.toString(), is(equalTo(USER_ID)));
    }

    @Test
    public void test_B_GetPins() throws Exception {
        //Response response = client.getPins(PROJECT_SLUG);
        //assertThat(response, containsString("First pin"));
        //assertThat(response, containsString("Second pin"));
        //assertThat(response, containsString("Third pin"));
    }

    @Test
    public void test_B_GetProjectDetail() throws Exception {
        Response response = client.getProjectDetail(projectSlug);
        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        String responseMessage = response.readEntity(String.class);
        JSONObject jsonObject = (JSONObject) jsonParser.parse(responseMessage);
        String projectName = (String) jsonObject.get("title");
        String ownerId = (String) jsonObject.get("owner_id");
        assertThat(projectName, is(equalTo(PROJECT_NAME)));
        assertThat(ownerId, is(equalTo(USER_ID)));
    }

    /* UPDATE */

    /* DELETE */

}
