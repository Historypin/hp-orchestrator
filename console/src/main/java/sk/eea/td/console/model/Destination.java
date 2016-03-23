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

    /**
     * Returns destination with given format code.
     *
     * @param formatCode format code
     * @return destination with input format code or throws IllegalArgumentException of none could be found.
     */
    public static Destination getDestinationByFormatCode(String formatCode) {
        for (Destination destination : Destination.values()) {
            if (destination.getFormatCode().equals(formatCode)) {
                return destination;
            }
        }
        throw new IllegalArgumentException("Cannot find destination by given format code: " + formatCode);
    }
}
