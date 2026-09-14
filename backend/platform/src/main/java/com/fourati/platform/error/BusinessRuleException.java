package com.fourati.platform.error;


import org.springframework.http.HttpStatusCode;

/**
 * A domain rule was violated. The HTTP status, title and retryability come
 * from the {@link ErrorCode} catalog, so the throw site only names the rule.
 */
public class BusinessRuleException extends BusinessException {

    private static final long serialVersionUID = -7099151512253966224L;

    public BusinessRuleException(ErrorCode code, String message) {
        super(code.code(), message, code.status());
    }

    /** Overrides the catalog status for a specific throw site; prefer the two-argument form. */
    public BusinessRuleException(ErrorCode code, String message, HttpStatusCode status) {
        super(code.code(), message, status);
    }
}
