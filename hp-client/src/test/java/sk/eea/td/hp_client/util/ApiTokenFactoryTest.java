package sk.eea.td.hp_client.util;

import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ApiTokenFactoryTest {

    public static final String API_KEY = "e519f7e8481e2002f9874cf10ac19bfca533704746d02b98ee9f9f6baa3231d8";

    public static final String API_SECRET = "4a37d542252bd03300a8e79131f9624c";

    public static final String EXPECTED_API_TOKEN = "0083a327ee8f63e9f57829dc58214bd63a078c7fcd69f369d54e4fd28a7df7d3";

    @Test(expected = RuntimeException.class)
    public void testWrongKeyInConstructorFails() {
        ApiTokenFactory factory = new ApiTokenFactory(null);
    }

    @Test
    public void testGetApiToken() throws Exception {
        // this test case example is taken from HistoryPin documentation
        Map<String, String> data = new TreeMap<>();
        data.put("id", "19");
        data.put("api_path", "user/get_edit.json");
        data.put("example_variable", "123");
        data.put("api_key", API_KEY);

        ApiTokenFactory apiTokenFactory = new ApiTokenFactory(API_SECRET);
        assertThat(apiTokenFactory.getApiToken(data), is(equalTo(EXPECTED_API_TOKEN)));
    }
}
