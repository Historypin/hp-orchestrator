package sk.eea.td.onto_client.dto;

import java.util.ArrayList;
import java.util.List;

public class EnrichmentDTO {

    private List<String> tags = new ArrayList<>();
    private List<String> places = new ArrayList<>();

    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public List<String> getPlaces() {
        return places;
    }
    public void setPlaces(List<String> places) {
        this.places = places;
    }
}
