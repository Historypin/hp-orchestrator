package sk.eea.td.onto_client.impl;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;

import sk.eea.td.onto_client.dto.EnrichResponseDTO;
import sk.eea.td.onto_client.dto.EnrichmentDTO;
import sk.eea.td.server.PublishMockOntoServer;

public class OntoClientImplTest2 {

    @Test
    public void extract2ObjectIT() throws JsonParseException, IOException {

        final String BASE_URL = "http://efd.ontotext.com/enrichment/extract";
        
        String text = "The furniture shop at Hastings Seafront East Sussex UK was the location of Rocks Carriage Works in the 19th Century where high class horseless carriages were constructed and displayed for sale. In 1851 Rocks carriages were at the Exhibition in Hyde Park and were used by Queen Victoria";
        String uri = "http://v77-beta-3.historypin-hrd.appspot.com/en/explore/pin/4125";
        System.out.println("base_url: " + BASE_URL);
        EnrichResponseDTO dto = new OntoClientImpl(BASE_URL, null).extract2Object(text, uri);
        System.out.println(dto);
    }

    @Ignore
    @Test
    public void extract2ObjectMock() throws JsonParseException, IOException {

        final String BASE_URL = "http://localhost:9000/enrichment/extract";

        PublishMockOntoServer server = new PublishMockOntoServer();
        server.start();

        String text = "The furniture shop at Hastings Seafront East Sussex UK was the location of Rocks Carriage Works in the 19th Century where high class horseless carriages were constructed and displayed for sale. In 1851 Rocks carriages were at the Exhibition in Hyde Park and were used by Queen Victoria";
        String uri = "http://v77-beta-3.historypin-hrd.appspot.com/en/explore/pin/4125";
        System.out.println("base_url: " + BASE_URL);
        EnrichResponseDTO dto = new OntoClientImpl(BASE_URL, null).extract2Object(text, uri);
        System.out.println(dto);

        server.stop();
    }

    @Test
    public void extractUsingJsonLDParser() throws JsonParseException, IOException {

        final String BASE_URL = "http://efd.ontotext.com/enrichment/extract";
        
        String text = "The furniture shop at Hastings Seafront East Sussex UK was the location of Rocks Carriage Works in the 19th Century where high class horseless carriages were constructed and displayed for sale. In 1851 Rocks carriages were at the Exhibition in Hyde Park and were used by Queen Victoria";
        String uri = "http://v77-beta-3.historypin-hrd.appspot.com/en/explore/pin/4125";
        System.out.println("base_url: " + BASE_URL);
        EnrichmentDTO result = new OntoClientImpl(BASE_URL, null).extractUsingJsonLDParser(text, uri);
        System.out.println(result);
    }
}
