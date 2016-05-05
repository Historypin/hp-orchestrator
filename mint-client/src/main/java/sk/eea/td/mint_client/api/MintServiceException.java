package sk.eea.td.mint_client.api;

public class MintServiceException extends Exception {

	private static final long serialVersionUID = 1L;

	private MintServiceException() {
		super();
	}

	public MintServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MintServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public MintServiceException(String message) {
		super(message);
	}

	public MintServiceException(Throwable cause) {
		super(cause);
	}

}
