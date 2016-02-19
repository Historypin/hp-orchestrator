package sk.eea.td.rest.model;

public class HarvestResponse {

    private String message;

    public HarvestResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
