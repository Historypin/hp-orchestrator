package sk.eea.td.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ServiceException extends Exception {

    private static final long serialVersionUID = -6787524870871338688L;

    private List<Error> errors = new ArrayList<>();

    public ServiceException(List<Error> errors) {
        super();
        this.errors = errors;
    }
    public ServiceException(Error error) {
        List<Error> errors = new ArrayList<>();
        errors.add(error);
        this.errors = errors;
    }
    public List<Error> getErrors() {
        return errors;
    }

    public static class Error {

        private Path path;
        private ErrorCode errorCode;

        public Error(Path path, ErrorCode errorCode) {
            this.path = path;
            this.errorCode = errorCode;
        }
        public Path getPath() {
            return path;
        }
        public ErrorCode getErrorCode() {
            return errorCode;
        }

        public static enum ErrorCode {
            FAILED_TO_SAVE_FILE,
            FAILED_TO_PARSE_JSON_FROM_STRING,
            FAILED_TO_LOAD_JSON_FROM_FILE,
            FAILED_TO_FIND_LOCAL_FILENAME_IN_JSON,
            FAILED_TO_DELETE_FILE,
            CHECKSUM_CHANGED,
            INVALID_JSON,
            FAILED_TO_LOAD_FILE_FOR_CHECKSUM,
            CLIENT_REQUEST_FAILED,
            JOB_ALREADY_CLOSED,
            FAILED_TO_CREATE_DIR
        }

    }
}
