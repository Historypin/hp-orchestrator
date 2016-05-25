package sk.eea.td.onto_client.impl;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;

import sk.eea.td.onto_client.dto.EnrichResponseDTO;

public class OntoClientImplTest2 {

    private static final String BASE_URL = "http://efd.ontotext.com/enrichment/extract";

    @Test
    public void extract2ObjectIT() throws JsonParseException, IOException {
        String text = "The furniture shop at Hastings Seafront East Sussex UK was the location of Rocks Carriage Works in the 19th Century where high class horseless carriages were constructed and displayed for sale. In 1851 Rocks carriages were at the Exhibition in Hyde Park and were used by Queen Victoria";
        String uri = "http://v77-beta-3.historypin-hrd.appspot.com/en/explore/pin/4125";
        System.out.println("base_url: " + BASE_URL);
        EnrichResponseDTO dto = new OntoClientImpl(BASE_URL, null).extract2Object(text, uri);
        System.out.println(dto);
    }
}
