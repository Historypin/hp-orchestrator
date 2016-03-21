package sk.eea.td.console.model;

public enum Destination {
    HP("hp.json"), TAGAPP("unknown"), MINT("unknown"), EUROPEANA("unknown"), SD("unknown");

    private String formatCode;

    Destination(String formatCode) {
        this.formatCode = formatCode;
    }

    public String getFormatCode() {
        return formatCode;
    }
}
