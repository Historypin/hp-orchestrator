package sk.eea.td.onto_client.impl;

import org.junit.Before;
import org.junit.Test;
import sk.eea.td.onto_client.api.OntoClient;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class OntoClientImplTestIT {

    private static final String TEXT = "This text is related to the London, capitol of UK.";

    private static final String BASE_URL = "http://tag.ontotext.com";

    private static final String WS_KEY = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwidXNlcm5hbWUiOiJ1c2VyIn0.JMV-kAdLd9RhGcxeCgqCc0O5xG9-oQUUwI4vC83BpwU";

    private OntoClient ontoClient;

    @Before
    public void setUp() throws Exception {
        ontoClient = new OntoClientImpl(BASE_URL, WS_KEY);
    }

    @Test
    public void testExtractCoordinatesFromTextByRelevance() throws Exception {
        String coordinates = ontoClient.extractCoordinatesFromTextByRelevance(TEXT);
        assertThat(coordinates, is(notNullValue()));
    }
}
