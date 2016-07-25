package sk.eea.td.console.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("blobParam")
public class BlobParam extends Param {

    @Column
    private String blobName;

    @Column
    private byte[] blobData;

    public BlobParam() {
        super();
    }

    public BlobParam(ParamKey key, String blobName, byte[] blobData) {
        super(key);
        this.blobName = blobName;
        this.blobData = blobData;
    }

    public BlobParam(ParamKey key, Job job, String blobName, byte[] blobData) {
        super(key, job);
        this.blobName = blobName;
        this.blobData = blobData;
    }

    public String getBlobName() {
        return blobName;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }

    public byte[] getBlobData() {
        return blobData;
    }

    public void setBlobData(byte[] blobData) {
        this.blobData = blobData;
    }
}
