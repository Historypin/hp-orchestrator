package sk.eea.td.hp_client.api;

public class Location {

    private Double lat;

    private Double lng;

    private Long range;

    public Location() {
    }

    public Location(Double lat, Double lng, Long range) {
        this.lat = lat;
        this.lng = lng;
        this.range = range;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Long getRange() {
        return range;
    }

    public void setRange(Long range) {
        this.range = range;
    }

    @Override public String toString() {
        return "Location{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", range=" + range +
                '}';
    }
}
