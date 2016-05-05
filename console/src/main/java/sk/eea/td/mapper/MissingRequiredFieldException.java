package sk.eea.td.mapper;

public class MissingRequiredFieldException extends Exception {

    public MissingRequiredFieldException(String fieldName, String remoteId) {
        super(String.format("Item with remoteId: '%s' is missing required field: '%s'.", remoteId, fieldName));
    }
}
