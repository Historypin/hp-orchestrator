package sk.eea.td.console.model;

import javax.ws.rs.core.MediaType;

public enum Destination {
    HP("hp.json", MediaType.APPLICATION_JSON), TAGAPP("unknown",MediaType.APPLICATION_OCTET_STREAM), MINT("mint.json",MediaType.APPLICATION_JSON), EUROPEANA("eu.json",MediaType.APPLICATION_JSON), SD("unknown",MediaType.APPLICATION_OCTET_STREAM), EUROPEANA_ANNOTATION("euoa.xml",MediaType.APPLICATION_XML);

    private String formatCode;
	private MediaType mediaType;

    Destination(String formatCode, String mt) {
        this.formatCode = formatCode;
        this.mediaType = MediaType.valueOf(mt);
    }

    public String getFormatCode() {
        return formatCode;
    }
    
    public MediaType getMediaType() {
    	return mediaType;
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
    
    /**
     * Returns transformer description from 'fromDestination' to 'targetDestination'.
     */
    public static String getTransformer(Destination fromDestination, Destination targetDestination){
    	return new StringBuilder(fromDestination.getFormatCode()).append("2").append(targetDestination.getFormatCode()).toString();
    }
}
