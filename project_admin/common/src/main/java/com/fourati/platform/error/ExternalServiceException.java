package com.fourati.platform.error;

/**
 * Thrown when a downstream / external service call fails.
 * Maps to 503 Service Unavailable so the client knows to retry.
 *
 * Example:
 *   throw new ExternalServiceException("payment-gateway", "Connection timed out after 5s");
 */
public class ExternalServiceException extends RuntimeException {

    private static final long serialVersionUID = -3355953109847107089L;
	private final String serviceName;

    public ExternalServiceException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
    }

	public String getServiceName() {
		return serviceName;
	}

}
