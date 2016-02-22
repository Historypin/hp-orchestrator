package ORG.oclc.oai.harvester2.verb;

import java.io.IOException;

public class HttpResponseCodeException extends IOException {
    
    private static final long serialVersionUID = 6757788450083857528L;
    private int responseCode;

    public HttpResponseCodeException(int responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }
    
}
