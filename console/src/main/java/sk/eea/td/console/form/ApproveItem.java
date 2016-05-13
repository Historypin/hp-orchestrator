package sk.eea.td.console.form;

import java.util.List;

public class ApproveItem {

    private String name;

    private String description;

    private String linkTo;

    private List<Tag> tags;

    private List<Place> places;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLinkTo() {
        return linkTo;
    }

    public void setLinkTo(String linkTo) {
        this.linkTo = linkTo;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }

    @Override
    public String toString() {
        return "ApproveItem{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", linkTo='" + linkTo + '\'' +
                ", tags=" + tags +
                ", places=" + places +
                '}';
    }
}
