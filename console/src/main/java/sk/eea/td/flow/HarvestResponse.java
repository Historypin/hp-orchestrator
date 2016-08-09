package sk.eea.td.flow;

import java.nio.file.Path;

public class HarvestResponse {

    private Path harvestPath;

    private boolean allItemHarvested;

    public HarvestResponse() {
    }

    public HarvestResponse(Path harvestPath, boolean allItemHarvested) {
        this.harvestPath = harvestPath;
        this.allItemHarvested = allItemHarvested;
    }

    public Path getHarvestPath() {
        return harvestPath;
    }

    public void setHarvestPath(Path harvestPath) {
        this.harvestPath = harvestPath;
    }

    public boolean isAllItemHarvested() {
        return allItemHarvested;
    }

    public void setAllItemHarvested(boolean allItemHarvested) {
        this.allItemHarvested = allItemHarvested;
    }
}
