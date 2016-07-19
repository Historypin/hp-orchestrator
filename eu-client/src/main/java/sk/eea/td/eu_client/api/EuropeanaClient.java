package sk.eea.td.eu_client.api;

import java.io.IOException;
import java.util.List;

public interface EuropeanaClient {

    String getRecord(String id) throws IOException, InterruptedException;

    String getRecordsEdmIsShownBy(String id) throws IOException, InterruptedException;

    String harvest(String luceneQuery, String facet, String cursor, String profile) throws IOException, InterruptedException;

    List<String> search(String luceneQuery) throws IOException, InterruptedException;

    List<String> search(String luceneQuery, String facet) throws IOException, InterruptedException;    
}
