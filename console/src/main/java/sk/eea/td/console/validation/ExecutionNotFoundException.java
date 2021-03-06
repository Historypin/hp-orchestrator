package sk.eea.td.console.validation;

public class ExecutionNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -272206707146405541L;
    private Long executionId;

    public ExecutionNotFoundException(Long executionId, String message) {
        super(message);
        this.executionId = executionId;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public void setExecutionId(Long executionId) {
        this.executionId = executionId;
    }
}
