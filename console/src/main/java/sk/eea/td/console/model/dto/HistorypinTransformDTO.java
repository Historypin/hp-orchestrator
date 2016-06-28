package sk.eea.td.console.model.dto;

import sk.eea.td.hp_client.api.Pin;

import java.util.List;

public class HistorypinTransformDTO {

    List<Pin> pins;

    public List<Pin> getPins() {
        return pins;
    }

    public void setPins(List<Pin> pins) {
        this.pins = pins;
    }

    @Override public String toString() {
        return "HistorypinTransformDTO{" +
                "pins=" + pins +
                '}';
    }
}
