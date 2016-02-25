package sk.eea.td.eu_client.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest({ EuropeanaClientImpl.class })
@RunWith(PowerMockRunner.class)
public class EuropeanaClientImplTest {

    private EuropeanaClientImpl europeanaClient;

    private Response response;

    @Before
    public void setUp() throws IllegalAccessException {
        Client client = mock(Client.class);
        WebTarget webTarget = mock(WebTarget.class);
        Invocation.Builder builder = mock(Invocation.Builder.class);
        response = mock(Response.class);

        when(builder.get()).thenReturn(response);
        when(webTarget.request()).thenReturn(builder);
        when(webTarget.path(anyString())).thenReturn(webTarget);
        when(webTarget.queryParam(anyString(), anyObject())).thenReturn(webTarget);
        when(client.target(anyString())).thenReturn(webTarget);

        europeanaClient = new EuropeanaClientImpl("", "", 1, 1);
        MemberModifier
                .field(EuropeanaClientImpl.class, "client").set(
                europeanaClient, client);

    }

    @Test
    public void testChainedCalls() throws IOException, InterruptedException {
        when(response.readEntity(String.class)).thenReturn("{\"nextCursor\": \"1\"}", "{\"nextCursor\": \"2\"}", "{\"nextCursor\": \"3\"}", "{}");
        List<String> results = europeanaClient.search(anyString());
        assertThat(results.size(), is(equalTo(4)));
    }

    @Test
    public void testRetrySuccess() throws IOException, InterruptedException {
        when(response.readEntity(String.class)).thenReturn("{\"nextCursor\": \"1\"}").thenThrow(new ProcessingException("This is horrible processing exception")).thenReturn("{}");
        List<String> results = europeanaClient.search(anyString());
        assertThat(results.size(), is(equalTo(2)));
    }

    @Test(expected = ProcessingException.class)
    public void testRetryFails() throws IOException, InterruptedException {
        when(response.readEntity(String.class)).thenReturn("{\"nextCursor\": \"1\"}").thenThrow(new ProcessingException("This is horrible processing exception")).thenThrow(new ProcessingException("This is even more horrible processing exception!"));
        europeanaClient.search(anyString());
        assertTrue(false); // should never reach
    }

}
