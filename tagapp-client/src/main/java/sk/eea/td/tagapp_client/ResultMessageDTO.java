package sk.eea.td.tagapp_client;

import java.text.MessageFormat;

public class ResultMessageDTO {

	public enum Status {
		/** Process succeeded. */
		SUCCESS,
		/** Process failed */
		FAILED;
	}
	private Status status;
	private String message;

	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message, Object ...objects) {
		this.message = MessageFormat.format(message, objects);
	}
}
