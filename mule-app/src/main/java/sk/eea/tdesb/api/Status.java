package sk.eea.tdesb.api;

public enum Status {
	/** Everything went OK */
	OK,
	/** Several issues have appeared, but it is possible to continue processing */
	WARN,
	/** Issues have appeared, need to try next time */
	ERROR, 
	/** Issues have appeared, which are not recoverable without admin intervene */ 
	FATAL;
}
