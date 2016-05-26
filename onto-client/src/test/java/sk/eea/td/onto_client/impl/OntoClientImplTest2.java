package sk.eea.td.onto_client.impl;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;

import sk.eea.td.onto_client.dto.EnrichResponseDTO;

public class OntoClientImplTest2 {

    private static final String BASE_URL = "http://efd.ontotext.com/enrichment/extract";

    @Test
    public void extract2ObjectIT() throws JsonParseException, IOException {
        String text = "A piece of strawberry sponge cake on a white plate with a small blue and white spotted mug of black coffee credit";
        String uri = "http://mint-projects.image.ntua.gr/data/foodanddrink/EUFD105370";
        System.out.println("base_url: " + BASE_URL);
        EnrichResponseDTO dto = new OntoClientImpl(BASE_URL, null).extract2Object(text, uri);
        System.out.println(dto);
    }
}
