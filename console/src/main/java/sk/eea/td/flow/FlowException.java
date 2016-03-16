package sk.eea.td.flow;

public class FlowException extends Exception {

	public FlowException() {
		super();
	}

	public FlowException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FlowException(String message, Throwable cause) {
		super(message, cause);
	}

	public FlowException(String message) {
		super(message);
	}

	public FlowException(Throwable cause) {
		super(cause);
	}

}
