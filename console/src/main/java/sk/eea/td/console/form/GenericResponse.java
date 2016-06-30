package sk.eea.td.console.form;

public class GenericResponse {

    private String status;

    private String errorMessages;

    public GenericResponse() {
    }

    public GenericResponse(String status) {
        this.status = status;
    }

    public GenericResponse(String status, String errorMessages) {
        this.status = status;
        this.errorMessages = errorMessages;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(String errorMessages) {
        this.errorMessages = errorMessages;
    }

    @Override
    public String toString() {
        return "JsonResponse{" +
                "status='" + status + '\'' +
                ", errorMessages='" + errorMessages + '\'' +
                '}';
    }
}
