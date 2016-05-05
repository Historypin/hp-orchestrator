package sk.eea.td.console.form;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.springframework.format.annotation.DateTimeFormat;
import sk.eea.td.console.model.Destination;
import sk.eea.td.console.validation.TaskFormValidationSequenceProvider;
import sk.eea.td.rest.validation.EuropeanaValidation;
import sk.eea.td.rest.validation.HistorypinTargetValidation;
import sk.eea.td.rest.validation.HistorypinValidation;
import sk.eea.td.rest.validation.OaipmhValidation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@GroupSequenceProvider(value = TaskFormValidationSequenceProvider.class)
public class TaskForm {

    @NotEmpty(message = "Name is missing.")
    private String name;

    @NotNull
    private Harvesting harvesting;

    @NotNull
    private Type type;

    @NotEmpty(message = "At least one destination is required.")
    private List<Destination> destinations;

    @NotNull(message = "Historypin user ID is missing.", groups = { HistorypinTargetValidation.class })
    private Long historypinUserId;

    @NotEmpty(message = "Historypin API Key is missing.", groups = { HistorypinTargetValidation.class })
    private String historypinApiKey;

    @NotEmpty(message = "Historypin API Secret is missing.", groups = { HistorypinTargetValidation.class })
    private String historypinApiSecret;

    @NotNull(message = "Target collection name is missing.", groups = { HistorypinTargetValidation.class })
    @Size(min = 6, max = 150, groups = { HistorypinTargetValidation.class })
    private String collectionName;

    @NotNull(message = "Target collection location (latitude) is missing.", groups = { HistorypinTargetValidation.class })
    private Double collectionLat = 46.517482; // default value

    @NotNull(message = "Target collection location (longitude) is missing.", groups = { HistorypinTargetValidation.class })
    private Double collectionLng = 8.1034214; // default value

    @NotNull(message = "Target collection location (radius) is missing.", groups = { HistorypinTargetValidation.class })
    private Long collectionRadius = 600000L; // default value

    @NotEmpty(message = "Default collection date is missing.", groups = { HistorypinTargetValidation.class })
    private String collectionDate;

    // this field is optional
    private String collectionTags;

    @NotNull(message = "Lucene query is missing.", groups = { EuropeanaValidation.class })
    @Size(min = 1, max = 150, groups = { EuropeanaValidation.class })
    private String luceneQuery;

    // this field is optional
    private String searchFacet;

    @NotNull(message = "Project slug is missing.", groups = { HistorypinValidation.class })
    @Size(min = 1, max = 150, groups = { HistorypinValidation.class })
    private String projectSlug;

    @NotNull(message = "From date is missing.", groups = { OaipmhValidation.class })
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    private Date oaiFrom;

    @NotNull(message = "Until date is missing.", groups = { OaipmhValidation.class })
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    private Date oaiUntil;

    @NotNull(message = "Set name is missing.", groups = { OaipmhValidation.class })
    @Size(min = 1, max = 150, groups = { OaipmhValidation.class })
    private String oaiSet;

    public enum Harvesting {
        EU, HP, OT
    }

    public enum Type {
        OAIPMH, REST
    }

    public Harvesting getHarvesting() {
        return harvesting;
    }

    public void setHarvesting(Harvesting harvesting) {
        this.harvesting = harvesting;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
    }

    public String getLuceneQuery() {
        return luceneQuery;
    }

    public void setLuceneQuery(String luceneQuery) {
        this.luceneQuery = luceneQuery;
    }

    public String getProjectSlug() {
        return projectSlug;
    }

    public void setProjectSlug(String projectSlug) {
        this.projectSlug = projectSlug;
    }

    public Date getOaiFrom() {
        return oaiFrom;
    }

    public void setOaiFrom(Date oaiFrom) {
        this.oaiFrom = oaiFrom;
    }

    public Date getOaiUntil() {
        return oaiUntil;
    }

    public void setOaiUntil(Date oaiUntil) {
        this.oaiUntil = oaiUntil;
    }

    public String getOaiSet() {
        return oaiSet;
    }

    public void setOaiSet(String oaiSet) {
        this.oaiSet = oaiSet;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public Double getCollectionLat() {
        return collectionLat;
    }

    public void setCollectionLat(Double collectionLat) {
        this.collectionLat = collectionLat;
    }

    public Double getCollectionLng() {
        return collectionLng;
    }

    public void setCollectionLng(Double collectionLng) {
        this.collectionLng = collectionLng;
    }

    public Long getCollectionRadius() {
        return collectionRadius;
    }

    public void setCollectionRadius(Long collectionRadius) {
        this.collectionRadius = collectionRadius;
    }

    public Long getHistorypinUserId() {
        return historypinUserId;
    }

    public void setHistorypinUserId(Long historypinUserId) {
        this.historypinUserId = historypinUserId;
    }

    public String getHistorypinApiKey() {
        return historypinApiKey;
    }

    public void setHistorypinApiKey(String historypinApiKey) {
        this.historypinApiKey = historypinApiKey;
    }

    public String getHistorypinApiSecret() {
        return historypinApiSecret;
    }

    public void setHistorypinApiSecret(String historypinApiSecret) {
        this.historypinApiSecret = historypinApiSecret;
    }

    public String getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(String collectionDate) {
        this.collectionDate = collectionDate;
    }

    public String getCollectionTags() {
        return collectionTags;
    }

    public void setCollectionTags(String collectionTags) {
        this.collectionTags = collectionTags;
    }

    public String getSearchFacet() {
        return searchFacet;
    }

    public void setSearchFacet(String searchFacet) {
        this.searchFacet = searchFacet;
    }

    @Override public String toString() {
        return "TaskForm{" +
                "name='" + name + '\'' +
                ", harvesting=" + harvesting +
                ", type=" + type +
                ", destinations=" + destinations +
                ", historypinUserId=" + historypinUserId +
                ", historypinApiKey='" + historypinApiKey + '\'' +
                ", historypinApiSecret='" + historypinApiSecret + '\'' +
                ", collectionName='" + collectionName + '\'' +
                ", collectionLat=" + collectionLat +
                ", collectionLng=" + collectionLng +
                ", collectionRadius=" + collectionRadius +
                ", collectionDate='" + collectionDate + '\'' +
                ", collectionTags='" + collectionTags + '\'' +
                ", luceneQuery='" + luceneQuery + '\'' +
                ", searchFacet='" + searchFacet + '\'' +
                ", projectSlug='" + projectSlug + '\'' +
                ", oaiFrom=" + oaiFrom +
                ", oaiUntil=" + oaiUntil +
                ", oaiSet='" + oaiSet + '\'' +
                '}';
    }
}
