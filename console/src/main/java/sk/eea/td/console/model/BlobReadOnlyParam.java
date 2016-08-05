package sk.eea.td.console.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("blobReadOnlyParam")
public class BlobReadOnlyParam extends ReadOnlyParam<BlobReadOnlyParam> {

    @Column
    private String blobName;

    @Column
    private byte[] blobData;

    public BlobReadOnlyParam() {
        super();
    }

    public BlobReadOnlyParam(ParamKey key, String blobName, byte[] blobData) {
        super(key);
        this.blobName = blobName;
        this.blobData = blobData;
    }

    public BlobReadOnlyParam(ParamKey key, AbstractJobRun jobRun, String blobName, byte[] blobData) {
        super(key, jobRun);
        this.blobName = blobName;
        this.blobData = blobData;
    }

    public BlobReadOnlyParam(BlobParam param, AbstractJobRun jobRun) {
        super(param.getKey(), jobRun);
        this.blobName = param.getBlobName();
        this.blobData = param.getBlobData();
    }

    public byte[] getBlobData() {
        return blobData;
    }

    public void setBlobData(byte[] blobData) {
        this.blobData = blobData;
    }

    public String getBlobName() {
        return blobName;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }
    
    @Override
    public BlobReadOnlyParam newInstance() {
        return new BlobReadOnlyParam(this.getKey(), this.getJobRun(), this.getBlobName(), this.getBlobData());
    }
}
