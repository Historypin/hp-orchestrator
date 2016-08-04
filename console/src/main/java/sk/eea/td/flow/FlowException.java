package sk.eea.td.flow;

public class FlowException extends Exception {

    private static final long serialVersionUID = 1L;

    public FlowException() {
    }

    public FlowException(String message) {
        super(message);
    }

    public FlowException(String message, Throwable cause) {
        super(message, cause);
    }

    public FlowException(Throwable cause) {
        super(cause);
    }

    public FlowException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
