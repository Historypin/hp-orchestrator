package sk.eea.td.hp_client.api;

public class Pin {

    private PinnerType pinnerType;

    private String caption;

    private String description;

    private String date;

    private String license;

    private Location location;

    private String link;

    private String content;

    private String tags;

    private String remoteId;

    private String remoteProviderId;

    public Pin() {
    }

    public Pin(PinnerType pinnerType,
            String caption,
            String description,
            String date,
            String license,
            Location location,
            String link,
            String content,
            String tags,
            String remoteId, String remoteProviderId) {
        this.pinnerType = pinnerType;
        this.caption = caption;
        this.description = description;
        this.date = date;
        this.license = license;
        this.location = location;
        this.link = link;
        this.content = content;
        this.tags = tags;
        this.remoteId = remoteId;
        this.remoteProviderId = remoteProviderId;
    }

    public PinnerType getPinnerType() {
        return pinnerType;
    }

    public void setPinnerType(PinnerType pinnerType) {
        this.pinnerType = pinnerType;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public String getRemoteProviderId() {
        return remoteProviderId;
    }

    public void setRemoteProviderId(String remoteProviderId) {
        this.remoteProviderId = remoteProviderId;
    }

    @Override public String toString() {
        return "Pin{" +
                "pinnerType=" + pinnerType +
                ", caption='" + caption + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", license='" + license + '\'' +
                ", location=" + location +
                ", link='" + link + '\'' +
                ", content='" + content + '\'' +
                ", tags='" + tags + '\'' +
                ", remoteId='" + remoteId + '\'' +
                ", remoteProviderId='" + remoteProviderId + '\'' +
                '}';
    }
}
