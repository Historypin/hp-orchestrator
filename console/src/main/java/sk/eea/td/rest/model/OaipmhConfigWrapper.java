package sk.eea.td.rest.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@ApiModel
public class OaipmhConfigWrapper {

    @NotNull
    @Size(min = 1, max = 200)
    @ApiModelProperty(value = "URL of OAIHandler", required = true)
    private String baseURL;

    @NotNull
    @Size(min = 1, max = 200)
    @ApiModelProperty(value = "date to harvest from. (YYYY-MM-DDThh:mm:ssZ)", required = true)
    private String from;

    @NotNull
    @Size(min = 1, max = 200)
    @ApiModelProperty(value = "date to harvest until. (YYYY-MM-DDThh:mm:ssZ)", required = true)
    private String until;

    @NotNull
    @Size(min = 1, max = 200)
    @ApiModelProperty(value = "set to harvest from", required = true)
    private String set;

    @NotNull
    @Size(min = 1, max = 200)
    @ApiModelProperty(value = "format of metadata used", required = true, example = "edm")
    private String metadataPrefix;

    @ApiModelProperty(value = "OAI-PMH authorization string")
    private String authorizationString;

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getUntil() {
        return until;
    }

    public void setUntil(String until) {
        this.until = until;
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public void setMetadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
    }

    public String getAuthorizationString() {
        return authorizationString;
    }

    public void setAuthorizationString(String authorizationString) {
        this.authorizationString = authorizationString;
    }

    @Override public String toString() {
        return "OaipmhConfig{" +
                "baseURL='" + baseURL + '\'' +
                ", from='" + from + '\'' +
                ", until='" + until + '\'' +
                ", set='" + set + '\'' +
                ", metadataPrefix='" + metadataPrefix + '\'' +
                ", authorizationString='" + authorizationString + '\'' +
                '}';
    }
}
