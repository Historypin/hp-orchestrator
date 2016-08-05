package sk.eea.td.console.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("stringParam")
public class StringParam extends Param {

    @Column(nullable = true)
    private String stringValue;

    public StringParam() {
        super();
    }

    public StringParam(ParamKey key, String stringValue) {
        super(key);
        this.stringValue = stringValue;
    }

    public StringParam(ParamKey key, Job job, String stringValue) {
        super(key, job);
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
}
