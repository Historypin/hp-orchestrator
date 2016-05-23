package sk.eea.td.onto_client.api;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;

import sk.eea.td.onto_client.dto.EnrichResponseDTO;

public interface OntoClient {

    /**
     * Extracts GPS coordinates from input text.
     *
     * Client requests Ontotext for extracting of GPS coordinates from given text.
     * It finds most probable location based on the relevance. GPS coordinates are returned in format ("%d.%d°N %d.%d°W").
     * Return null if coordinates were not found.
     *
     * @param text Input text.
     * @return GPS coordinates or null.
     * @throws IOException
     */
    String extractCoordinatesFromTextByRelevance(String text) throws IOException;

    public String extract(String text, String uri) throws JsonParseException, IOException;
    public EnrichResponseDTO extract2Object(String text, String uri) throws JsonParseException, IOException;
}
