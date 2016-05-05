package sk.eea.td.onto_client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ExtractResponseDTO {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("dct:spatial")
    private List<IdObject> spatial;

    @JsonProperty("dct:subject")
    private List<IdObject> subject;

    public List<IdObject> getSpatial() {
        return spatial;
    }
    public void setSpatial(List<IdObject> spatial) {
        this.spatial = spatial;
    }
    public List<IdObject> getSubject() {
        return subject;
    }
    public void setSubject(List<IdObject> subject) {
        this.subject = subject;
    }

    public static class IdObject {

        @JsonProperty("@id")
        private String value;

        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }
}
