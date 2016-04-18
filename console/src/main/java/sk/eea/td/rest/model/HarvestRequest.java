package sk.eea.td.rest.model;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.group.GroupSequenceProvider;
import sk.eea.td.rest.validation.EuropeanaValidation;
import sk.eea.td.rest.validation.HarvestRequestValidationSequenceProvider;
import sk.eea.td.rest.validation.HistorypinValidation;
import sk.eea.td.rest.validation.OaipmhValidation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@GroupSequenceProvider(value = HarvestRequestValidationSequenceProvider.class)
@ApiModel
public class HarvestRequest {

    @NotNull
    @Size(min = 1, max = 150)
    @ApiModelProperty(value = "path to file, which will store harvested data", required = true)
    private String filePath;

    @NotNull
    @ApiModelProperty(value = "type of connector", allowableValues = "EUROPEANA, HISTORYPIN, OAIPMH", required = true)
    private Connector connector;

    @NotNull(message = "Lucene query is mandatory for EUROPEANA connector.", groups = { EuropeanaValidation.class})
    @Size(min = 1, max = 200)
    @ApiModelProperty(value = "Lucene query for EUROPEANA connector to harvest. Required if EUROPEANA connector is used!")
    private String luceneQuery;

    // this field is optional
    private String searchFacet;

    @NotNull(message = "Project slug is mandatory for HISTORYPIN connector.", groups = { HistorypinValidation.class})
    @Size(min = 1, max = 200)
    @ApiModelProperty(value = "projectSlug for HISTORYPIN connector to harvest. Required if HISTORYPIN connector is used!")
    private String projectSlug;

    @NotNull(message = "OAI-PMH configuration is mandatory for OAIPMH connector", groups = { OaipmhValidation.class})
    @Valid
    @ApiModelProperty(value = "configuration object for OAIPMH connector to harvest. Required if OAIPMH connector is used!")
    private OaipmhConfigWrapper oaipmhConfigWrapper;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
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

    public OaipmhConfigWrapper getOaipmhConfigWrapper() {
        return oaipmhConfigWrapper;
    }

    public void setOaipmhConfigWrapper(OaipmhConfigWrapper oaipmhConfigWrapper) {
        this.oaipmhConfigWrapper = oaipmhConfigWrapper;
    }

    public String getSearchFacet() {
        return searchFacet;
    }

    public void setSearchFacet(String searchFacet) {
        this.searchFacet = searchFacet;
    }

    @Override public String toString() {
        return "HarvestRequest{" +
                "filePath='" + filePath + '\'' +
                ", connector=" + connector +
                ", luceneQuery='" + luceneQuery + '\'' +
                ", searchFacet='" + searchFacet + '\'' +
                ", projectSlug='" + projectSlug + '\'' +
                ", oaipmhConfigWrapper=" + oaipmhConfigWrapper +
                '}';
    }
}
