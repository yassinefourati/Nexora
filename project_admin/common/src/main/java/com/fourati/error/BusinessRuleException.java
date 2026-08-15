package com.fourati.error;

import com.fourati.platform.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class BusinessRuleException extends BusinessException {

    private static final long serialVersionUID = -7099151512253966224L;

	public BusinessRuleException(ErrorCode code, String message) {
        super(code.code(), message, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    public BusinessRuleException(ErrorCode code, String message, HttpStatusCode status) {
        super(code.code(), message, status);
    }
}
