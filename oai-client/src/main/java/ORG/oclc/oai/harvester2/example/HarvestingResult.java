package ORG.oclc.oai.harvester2.example;

public class HarvestingResult {

    private int harvestedFiles;

    private int objectCount;

    private String status;

    public HarvestingResult(int harvestedFiles, int objectsCount, String status) {
        this.harvestedFiles = harvestedFiles;
        this.objectCount = objectsCount;
        this.status = status;
    }

    public int getHarvestedFiles() {
        return harvestedFiles;
    }

    public void setHarvestedFiles(int harvestedFiles) {
        this.harvestedFiles = harvestedFiles;
    }

    public int getObjectCount() {
        return objectCount;
    }

    public void setObjectCount(int objectCount) {
        this.objectCount = objectCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override public String toString() {
        return "HarvestingResult{" +
                "harvestedFiles=" + harvestedFiles +
                ", objectCount=" + objectCount +
                ", status='" + status + '\'' +
                '}';
    }
}
