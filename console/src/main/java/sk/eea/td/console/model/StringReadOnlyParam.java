package sk.eea.td.console.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("StringReadOnlyParam")
public class StringReadOnlyParam extends ReadOnlyParam<StringReadOnlyParam> {

    @Column(nullable = true)
    private String stringValue;

    public StringReadOnlyParam() {
        super();
    }

    public StringReadOnlyParam(ParamKey key, String stringValue) {
        super(key);
        this.stringValue = stringValue;
    }

    public StringReadOnlyParam(ParamKey key, AbstractJobRun jobRun, String stringValue) {
        super(key, jobRun);
        this.stringValue = stringValue;
    }

    public StringReadOnlyParam(StringParam param, AbstractJobRun jobRun) {
        super(param.getKey(), jobRun);
        this.stringValue = param.getStringValue();
    }


    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public StringReadOnlyParam newInstance() {
        return new StringReadOnlyParam(this.getKey(),this.getJobRun(), this.getStringValue()) ;
    }    
}
