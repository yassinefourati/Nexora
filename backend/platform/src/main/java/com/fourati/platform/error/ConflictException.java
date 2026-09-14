package com.fourati.platform.error;

public class ConflictException extends RuntimeException {

	private static final long serialVersionUID = -8369660078245413699L;

	public ConflictException(String message) {
		super(message);
	}

}
