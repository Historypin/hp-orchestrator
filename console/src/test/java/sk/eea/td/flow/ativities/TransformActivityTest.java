package sk.eea.td.flow.ativities;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.eea.td.console.model.Destination;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.nio.file.Path;
import java.nio.file.Paths;

@Ignore
public class TransformActivityTest {

    private static final Logger LOG = LoggerFactory.getLogger(TransformActivityTest.class);

    private String MULE_TRANSFORM_URL = "http://localhost:8081/api/transformData";

    private String PATH_TO_FILE = "/tmp/td/job_run_1852/harvest/1458309245270-673.eu.xml";

    private String PATH_TO_TRANSFORM = "/tmp/td/job_run_1852/transform/";

    private Client client;

    @Before
    public void setup() {
        ClientConfig clientConfig = new ClientConfig();
        this.client = ClientBuilder.newClient(clientConfig).register(MultiPartFeature.class);
    }

    @Test
    public void testExecute() {
        final WebTarget target = client.target(MULE_TRANSFORM_URL);
        final Destination destination = Destination.HP;

        Path file = Paths.get(PATH_TO_FILE);
        String sourceFormatCode = file.getFileName().toString().split("\\.", 2)[1];
        String destFormatCode = destination.getFormatCode();
        String transformer = String.format("%s2%s", sourceFormatCode, destFormatCode);
        System.out.println(transformer);

        //		final WebTarget target = client.target(MULE_TRANSFORM_URL);
        //		final Destination destination = Destination.HP;
        //		Response response = target.queryParam("transformation", "").request(MediaType.APPLICATION_JSON, MediaType.TEXT_XML).post(Entity.entity(Paths.get(PATH_TO_FILE).toFile(), MediaType.TEXT_XML));
        //		try (InputStream inputStream = response.readEntity(InputStream.class)) {
        //			Path transformedFile = PathUtils.createUniqueFilename(Paths.get(PATH_TO_TRANSFORM), destination.getFormatCode());
        //			Files.copy(inputStream, transformedFile);
        //			LOG.debug("File '%s' has been transformed into file: '%s'", PATH_TO_FILE, transformedFile.toString());
        //		} catch (IOException e) {
        //			LOG.error(String.format("Exception at transforming file: %s", PATH_TO_FILE), e);
        //		}
    }

}
