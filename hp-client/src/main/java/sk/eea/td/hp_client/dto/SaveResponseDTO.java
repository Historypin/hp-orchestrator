package sk.eea.td.hp_client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class SaveResponseDTO {

    private Long id;

    private boolean success;

    @JsonProperty("new_project")
    private String newProject;

    private String slug;

    private List<Error> errors;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getNewProject() {
        return newProject;
    }

    public void setNewProject(String newProject) {
        this.newProject = newProject;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    @Override public String toString() {
        return "SaveResponseDTO{" +
                "id=" + id +
                ", success=" + success +
                ", newProject='" + newProject + '\'' +
                ", slug='" + slug + '\'' +
                ", errors=" + errors +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Error {

        public String type;

        public String message;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override public String toString() {
            return "Error{" +
                    "type='" + type + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
