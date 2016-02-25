package sk.eea.td.eu_client.impl;

import org.junit.Before;
import org.junit.Test;
import sk.eea.td.eu_client.api.EuropeanaClient;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class EuropeanaClientImplIT {

    private String BASE_URL = "http://www.europeana.eu";

    private String WS_KEY = "api2demo";

    private Integer MAX_RETRIES = 2;

    private Integer RETRY_DELAY = 1;

    private String LUCENE_QUERY = "timestamp_created:[2013-11-01T00:00:0.000Z TO 2013-11-12T16:36:01.000Z]";

    private EuropeanaClient europeanaClient;

    @Before
    public void setUp() throws Exception {
        this.europeanaClient = new EuropeanaClientImpl(BASE_URL, WS_KEY, MAX_RETRIES, RETRY_DELAY);
    }

    @Test
    public void test() throws IOException, InterruptedException {
        List<String> jsons = europeanaClient.search(LUCENE_QUERY);
        assertThat(jsons.size(), is(greaterThan(0)));
    }
}
