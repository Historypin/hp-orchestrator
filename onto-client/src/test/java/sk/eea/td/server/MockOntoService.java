package sk.eea.td.server;

import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;

import org.apache.commons.io.IOUtils;

@Path("/enrichment")
public class MockOntoService {

    public MockOntoService() {
        super();
    }

    @POST
    @Path("extract")
//    @Consumes("text/xml")
    @Produces("text/xml")
    public String extract(@Context Request request, String uri) throws IOException {
        System.out.println("received event:" + uri);
        return IOUtils.toString(getClass().getResourceAsStream("/extract-response-new.json"), "UTF-8");
    }

}
