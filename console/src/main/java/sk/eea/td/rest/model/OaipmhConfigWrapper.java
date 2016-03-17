package sk.eea.td.rest.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@ApiModel
public class OaipmhConfigWrapper {

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

    public OaipmhConfigWrapper() {
    }

    public OaipmhConfigWrapper(String from, String until, String set, String metadataPrefix) {
        this.from = from;
        this.until = until;
        this.set = set;
        this.metadataPrefix = metadataPrefix;
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

    @Override public String toString() {
        return "OaipmhConfigWrapper{" +
                "from='" + from + '\'' +
                ", until='" + until + '\'' +
                ", set='" + set + '\'' +
                ", metadataPrefix='" + metadataPrefix + '\'' +
                '}';
    }
}
