package com.fourati.platform.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Generic business-rule violation.
 * Use when the request is syntactically valid but violates domain logic
 * that doesn't fit ResourceNotFoundException or ConflictException.
 *
 * Example:
 *   throw new BusinessException("INVOICE_ALREADY_PAID",
 *       "Invoice #1234 has already been paid and cannot be modified");
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 6477788655267122028L;

    private final String errorCode;
    private final HttpStatusCode status;

    public BusinessException(String errorCode, String message) {
        this(errorCode, message, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    public BusinessException(String errorCode, String message, HttpStatusCode status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

	public String getErrorCode() {
		return errorCode;
	}

	public HttpStatusCode getStatus() {
		return status;
	}

}
