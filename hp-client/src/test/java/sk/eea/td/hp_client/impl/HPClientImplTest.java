package sk.eea.td.hp_client.impl;

import org.junit.Before;
import org.junit.Test;
import sk.eea.td.hp_client.api.HPClient;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class HPClientImplTest {

    public static final String URL = "http://v76-beta-1.historypin-hrd.appspot.com";

    public static final String PIN_ID = "657486";

    public static final String PROJECT_SLUG = "my-test-collection";

    private HPClient client;

    @Before
    public void init(){
        this.client = new HPClientImpl(URL);
    }

    @Test
    public void testGetPin() throws Exception {
        String response = client.getPin(PIN_ID);
        assertThat(response, containsString("Third pin"));
    }

    @Test
    public void testGetPins() throws Exception {
        String response = client.getPins(PROJECT_SLUG);
        assertThat(response, containsString("First pin"));
        assertThat(response, containsString("Second pin"));
        assertThat(response, containsString("Third pin"));
    }

    @Test
    public void testGetProjectDetail() throws Exception {
        String response = client.getProjectDetail(PROJECT_SLUG);
        assertThat(response, containsString("My test collection"));
    }
}
