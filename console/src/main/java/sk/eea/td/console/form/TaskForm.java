package sk.eea.td.console.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.springframework.web.multipart.MultipartFile;

import sk.eea.td.console.model.Flow;
import sk.eea.td.console.validation.TaskFormValidationSequenceProvider;
import sk.eea.td.rest.validation.CsvFileValidation;
import sk.eea.td.rest.validation.Flow1Validation;
import sk.eea.td.rest.validation.Flow2Validation;
import sk.eea.td.rest.validation.Flow4Validation;
import sk.eea.td.rest.validation.Flow5Validation;
import sk.eea.td.rest.validation.Flow6Validation;
import sk.eea.td.rest.validation.LuceneQueryValidation;

@GroupSequenceProvider(value = TaskFormValidationSequenceProvider.class)
public class TaskForm {

    public enum HarvestType {
        LUCENE_QUERY, CSV_FILE
    }

    private Long jobId;

    @NotNull(message = "Flow is required")
    private Flow flow = Flow.FLOW_1; // default value

    @NotEmpty(message = "Name is missing.")
    private String name;

    @NotNull(message = "Historypin user ID is missing.", groups = { Flow1Validation.class })
    private Long historypinUserId;

    @NotEmpty(message = "Historypin API Key is missing.", groups = { Flow1Validation.class })
    private String historypinApiKey;

    @NotEmpty(message = "Historypin API Secret is missing.", groups = { Flow1Validation.class })
    private String historypinApiSecret;

    @NotNull(message = "Target collection name is missing.", groups = { Flow1Validation.class })
    @Size(min = 6, max = 150, groups = { Flow1Validation.class })
    private String collectionName;

    @NotNull(message = "Target collection location (latitude) is missing.", groups = { Flow1Validation.class })
    private Double collectionLat = 46.517482; // default value

    @NotNull(message = "Target collection location (longitude) is missing.", groups = { Flow1Validation.class })
    private Double collectionLng = 8.1034214; // default value

    @NotNull(message = "Target collection location (radius) is missing.", groups = { Flow1Validation.class })
    private Long collectionRadius = 600000L; // default value

    @NotEmpty(message = "Default collection date is missing.", groups = { Flow1Validation.class })
    private String collectionDate;

    // this field is optional
    private String collectionTags;

    @NotNull(message = "Harvest type needs to be chosen.", groups = {Flow1Validation.class, Flow6Validation.class})
    private HarvestType harvestType = HarvestType.LUCENE_QUERY;

    private String csvFileName;

    @NotNull(message = "CSV file need to be provided.", groups = {CsvFileValidation.class})
    private MultipartFile csvFile;

    @NotNull(message = "Lucene query is missing.", groups = { LuceneQueryValidation.class })
    @Size(min = 1, max = 300, groups = { LuceneQueryValidation.class })
    private String luceneQuery;

    // this field is optional
    private String searchFacet;

    @NotNull(message = "Project slug is missing.", groups = { Flow2Validation.class, Flow5Validation.class})
    @Size(min = 1, max = 150, groups = { Flow2Validation.class, Flow5Validation.class })
    private String projectSlug;

    @NotNull(message = "Date from is missing.", groups = { Flow4Validation.class })
    private String dateFrom;

    @NotNull(message = "Date until is missing.", groups = { Flow4Validation.class })
    private String dateUntil;

    public Flow getFlow() {
        return flow;
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateUntil() {
        return dateUntil;
    }

    public void setDateUntil(String dateUntil) {
        this.dateUntil = dateUntil;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public HarvestType getHarvestType() {
        return harvestType;
    }

    public void setHarvestType(HarvestType harvestType) {
        this.harvestType = harvestType;
    }

    public MultipartFile getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(MultipartFile csvFile) {
        this.csvFile = csvFile;
    }

    public String getCsvFileName() {
        return csvFileName;
    }

    public void setCsvFileName(String csvFileName) {
        this.csvFileName = csvFileName;
    }

    @Override
    public String toString() {
        return "TaskForm{" +
                "jobId=" + jobId +
                ", flow=" + flow +
                ", name='" + name + '\'' +
                ", historypinUserId=" + historypinUserId +
                ", historypinApiKey='" + historypinApiKey + '\'' +
                ", historypinApiSecret='" + historypinApiSecret + '\'' +
                ", collectionName='" + collectionName + '\'' +
                ", collectionLat=" + collectionLat +
                ", collectionLng=" + collectionLng +
                ", collectionRadius=" + collectionRadius +
                ", collectionDate='" + collectionDate + '\'' +
                ", collectionTags='" + collectionTags + '\'' +
                ", harvestType=" + harvestType +
                ", csvFileName='" + csvFileName + '\'' +
                ", csvFile=" + csvFile +
                ", luceneQuery='" + luceneQuery + '\'' +
                ", searchFacet='" + searchFacet + '\'' +
                ", projectSlug='" + projectSlug + '\'' +
                ", dateFrom='" + dateFrom + '\'' +
                ", dateUntil='" + dateUntil + '\'' +
                '}';
    }
}
