package sk.eea.td.console.form;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.group.GroupSequenceProvider;
import sk.eea.td.console.validation.TaskFormValidationSequenceProvider;
import sk.eea.td.rest.model.Connector;
import sk.eea.td.rest.validation.EuropeanaValidation;
import sk.eea.td.rest.validation.HistorypinTargetValidation;
import sk.eea.td.rest.validation.HistorypinValidation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@GroupSequenceProvider(value = TaskFormValidationSequenceProvider.class)
public class TaskForm {

    @NotEmpty(message = "Name is missing.")
    private String name;

    @NotNull(message = "Source is required.")
    private Connector source;

    @NotNull(message = "Target is required.")
    private Connector target;

    @NotEmpty(message = "Target is required.")
    private Connector connector;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Connector getSource() {
        return source;
    }

    public void setSource(Connector source) {
        this.source = source;
    }

    public Connector getTarget() {
        return target;
    }

    public void setTarget(Connector target) {
        this.target = target;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
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

    public String getLuceneQuery() {
        return luceneQuery;
    }

    public void setLuceneQuery(String luceneQuery) {
        this.luceneQuery = luceneQuery;
    }

    public String getSearchFacet() {
        return searchFacet;
    }

    public void setSearchFacet(String searchFacet) {
        this.searchFacet = searchFacet;
    }

    public String getProjectSlug() {
        return projectSlug;
    }

    public void setProjectSlug(String projectSlug) {
        this.projectSlug = projectSlug;
    }

    @Override public String toString() {
        return "TaskForm{" +
                "name='" + name + '\'' +
                ", source=" + source +
                ", target=" + target +
                ", connector=" + connector +
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
                '}';
    }
}
