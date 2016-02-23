package sk.eea.td.rest.model;

public class RestErrorResponse {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override public String toString() {
        return "RestErrorResponse{" +
                "message='" + message + '\'' +
                '}';
    }
}
