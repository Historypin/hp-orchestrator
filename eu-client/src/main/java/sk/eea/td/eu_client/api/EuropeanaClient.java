package sk.eea.td.eu_client.api;

import java.io.IOException;
import java.util.List;

public interface EuropeanaClient {

    List<String> search(String luceneQuery) throws IOException, InterruptedException;
}
