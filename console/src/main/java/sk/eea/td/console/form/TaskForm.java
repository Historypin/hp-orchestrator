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

    @NotEmpty(message = "Name is missing")
    private String name;

    @NotNull
    private Harvesting harvesting;

    @NotNull
    private Type type;

    @NotEmpty(message = "At least one destination is required.")
    private List<Destination> destinations;

    @NotNull(message = "Target collection name is missing", groups = { HistorypinTargetValidation.class })
    @Size(min = 1, max = 150, groups = { HistorypinTargetValidation.class })
    private String collectionName;

    @NotNull(message = "Lucene query is missing.", groups = { EuropeanaValidation.class })
    @Size(min = 1, max = 150, groups = { EuropeanaValidation.class })
    private String luceneQuery;

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

    @NotNull(message = "Metadata prefix is missing.", groups = { OaipmhValidation.class })
    @Size(min = 1, max = 150, groups = { OaipmhValidation.class })
    private String oaiMetadataPrefix;

    public enum Harvesting {
        EU, HP;
    }

    public enum Type {
        OAIPMH, REST;
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

    public String getOaiMetadataPrefix() {
        return oaiMetadataPrefix;
    }

    public void setOaiMetadataPrefix(String oaiMetadataPrefix) {
        this.oaiMetadataPrefix = oaiMetadataPrefix;
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

    @Override public String toString() {
        return "TaskForm{" +
                "name='" + name + '\'' +
                ", harvesting=" + harvesting +
                ", type=" + type +
                ", destinations=" + destinations +
                ", collectionName='" + collectionName + '\'' +
                ", luceneQuery='" + luceneQuery + '\'' +
                ", projectSlug='" + projectSlug + '\'' +
                ", oaiFrom=" + oaiFrom +
                ", oaiUntil=" + oaiUntil +
                ", oaiSet='" + oaiSet + '\'' +
                ", oaiMetadataPrefix='" + oaiMetadataPrefix + '\'' +
                '}';
    }
}
