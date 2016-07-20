package sk.eea.td.console.model.dto;

import java.util.List;

public class ReviewDTO {

    private Long id;
    
    private String externalId;

    private String caption;

    private String description;

    private String url;

    private String checksum;

    private String localFilename;

    private Boolean approved;

    private List<String> originalTags;

    private List<String> approvedTags;

    private List<String> originalPlaces;

    private List<String> approvedPlaces;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getLocalFilename() {
        return localFilename;
    }

    public void setLocalFilename(String localFilename) {
        this.localFilename = localFilename;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public List<String> getOriginalTags() {
        return originalTags;
    }

    public void setOriginalTags(List<String> originalTags) {
        this.originalTags = originalTags;
    }

    public List<String> getApprovedTags() {
        return approvedTags;
    }

    public void setApprovedTags(List<String> approvedTags) {
        this.approvedTags = approvedTags;
    }

    public List<String> getOriginalPlaces() {
        return originalPlaces;
    }

    public void setOriginalPlaces(List<String> originalPlaces) {
        this.originalPlaces = originalPlaces;
    }

    public List<String> getApprovedPlaces() {
        return approvedPlaces;
    }

    public void setApprovedPlaces(List<String> approvedPlaces) {
        this.approvedPlaces = approvedPlaces;
    }
    
    @Override
    public String toString() {
        return "ReviewDTO{" +
                "id=" + id +
                ", caption='" + caption + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", checksum='" + checksum + '\'' +
                ", localFilename='" + localFilename + '\'' +
                ", approved=" + approved +
                ", originalTags=" + originalTags +
                ", approvedTags=" + approvedTags +
                ", originalPlaces=" + originalPlaces +
                ", approvedPlaces=" + approvedPlaces +
                '}';
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
