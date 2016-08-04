package sk.eea.td.mapper;

public class MissingRequiredFieldException extends Exception {

    private static final long serialVersionUID = 1L;

    public MissingRequiredFieldException(String fieldName, String remoteId) {
        super(String.format("Item with remoteId: '%s' is missing required field: '%s'.", remoteId, fieldName));
    }
}
