package sk.eea.td.eu_client.api;

import java.io.IOException;

public interface EuropeanaAnnotationClient {

    String createAnnotation(String annotationJson) throws IOException, InterruptedException;
    
}
