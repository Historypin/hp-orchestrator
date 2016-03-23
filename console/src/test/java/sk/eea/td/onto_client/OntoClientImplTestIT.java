package sk.eea.td.onto_client;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import sk.eea.td.IntegrationTest;
import sk.eea.td.config.IntegrationTestConfig;
import sk.eea.td.onto_client.api.OntoClient;
import sk.eea.td.onto_client.impl.OntoClientImpl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationTestConfig.class)
public class OntoClientImplTestIT {

    private static final String TEXT = "This text is related to the London, capitol of UK.";

    @Value("${ontotext.base.url}")
    private String baseUrl;

    @Value("${ontotext.ws.key}")
    private String wsKey;

    private OntoClient ontoClient;

    @Before
    public void setUp() throws Exception {
        ontoClient = new OntoClientImpl(baseUrl, wsKey);
    }

    @Test
    public void testExtractCoordinatesFromTextByRelevance() throws Exception {
        String coordinates = ontoClient.extractCoordinatesFromTextByRelevance(TEXT);
        assertThat(coordinates, is(notNullValue()));
    }
}
