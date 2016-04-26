package sk.eea.td.onto_client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ExtractResponseDTO {

    private List<String> spatial;
    private List<String> subject;

    public List<String> getSpatial() {
        return spatial;
    }
    public void setSpatial(List<String> spatial) {
        this.spatial = spatial;
    }
    public List<String> getSubject() {
        return subject;
    }
    public void setSubject(List<String> subject) {
        this.subject = subject;
    }
}
