package sk.eea.td.rest.model;

public enum Connector {

    EUROPEANA("eu.json"), HISTORYPIN("hp.json"), OAIPMH("oai.xml"), ONTOTEXT("onto.json"), EUROPEANA_ANNOTATION("euoa.xml"), TAGAPP("tag.json"), MINT("mint.json");

    private String formatCode;

    Connector(String formatCode) {
        this.formatCode = formatCode;
    }

    /**
     * Returns connector with given format code.
     *
     * @param formatCode format code
     * @return connector with input format code or throws IllegalArgumentException of none could be found.
     */
    public static Connector getConnectorByFormatCode(String formatCode) {
        for (Connector connector : Connector.values()) {
            if (connector.getFormatCode().equals(formatCode)) {
                return connector;
            }
        }
        throw new IllegalArgumentException("Cannot find connector by given format code: " + formatCode);
    }

    public String getFormatCode() {
        return formatCode;
    }
}
