package sk.eea.td.server;

import java.io.IOException;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class PublishMockOntoServer {

    public static final String BASE_URI = "http://localhost:9000/";
    private HttpServer server;

    public PublishMockOntoServer() {
    }

    public void start() throws IOException {
        final ResourceConfig rc = new ResourceConfig(MockOntoService.class);
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        server.start();
    }

    public void stop() {
        server.shutdown();
    }
}
