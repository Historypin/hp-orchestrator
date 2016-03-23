package sk.eea.td.eu_client;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import sk.eea.td.IntegrationTest;
import sk.eea.td.config.IntegrationTestConfig;
import sk.eea.td.eu_client.api.EuropeanaClient;
import sk.eea.td.eu_client.impl.EuropeanaClientImpl;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationTestConfig.class)
public class EuropeanaClientImplIT {

    @Value("${europeana.base.url}")
    private String baseURL;

    @Value("${europeana.ws.key}")
    private String wsKey;

    @Value("${europeana.retry}")
    private Integer maxRetries;

    @Value("${europeana.retry.delay}")
    private Integer retryDelay;

    private String LUCENE_QUERY = "timestamp_created:[2013-11-01T00:00:0.000Z TO 2013-11-12T16:36:01.000Z]";

    private EuropeanaClient europeanaClient;

    @Before
    public void setUp() throws Exception {
        this.europeanaClient = new EuropeanaClientImpl(baseURL, wsKey, maxRetries, retryDelay);
    }

    @Test
    public void test() throws IOException, InterruptedException {
        List<String> jsons = europeanaClient.search(LUCENE_QUERY);
        assertThat(jsons.size(), is(greaterThan(0)));
    }
}
