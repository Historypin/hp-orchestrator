package sk.eea.td.onto_client.api;

import java.io.IOException;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.fasterxml.jackson.core.JsonParseException;

import sk.eea.td.onto_client.dto.EnrichResponseDTO;
import sk.eea.td.onto_client.dto.EnrichmentDTO;

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

    public EnrichmentDTO extractUsingJsonLDParser(String text, String uri) throws RDFParseException, RDFHandlerException, IOException;
}
