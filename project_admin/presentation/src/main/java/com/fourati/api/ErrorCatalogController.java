package com.fourati.api;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourati.platform.error.BusinessException;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ExternalServiceException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.platform.web.ApiResponse;
import com.fourati.common.ApiConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Error catalog — documents every error code the API can return.
 *
 * GET  /api/v1/errors → full catalog
 * GET  /api/v1/errors/{code} → single entry
 * GET  /api/v1/errors/simulate/{httpStatus} → deliberately triggers that HTTP error (dev/test only)
 */
@Profile("!prod")
@RestController
@RequestMapping(ApiConstants.VERSION +"/errors")
@Tag(name = "Error Catalog", description = """
    Complete catalog of all error codes the API can return.
    Use `/simulate/{status}` to test your frontend error-handling without needing real failures.
    """)
public class ErrorCatalogController {

    private static final List<ErrorEntry> CATALOG = List.of(

        // 4xx Client errors 

        new ErrorEntry(400, "VALIDATION_FAILED", "Request validation failed", "One or more fields in the request body failed validation. See 'violations' array.", false, null),
		new ErrorEntry(400, "MALFORMED_REQUEST_BODY", "The request body could not be parsed", "Check JSON syntax, field names, and value types.", false, null),
        new ErrorEntry(400, "TYPE_MISMATCH", "Parameter type mismatch", "A path or query parameter has the wrong type (e.g. 'abc' instead of a UUID).", false, null),
        new ErrorEntry(400, "MISSING_PARAMETER", "Required query parameter is missing", "Add the missing parameter to the request.", false, null),
        new ErrorEntry(400, "MISSING_HEADER", "Required request header is missing", "Add the missing header to the request.", false, null),
        new ErrorEntry(400, "INVALID_ARGUMENT", "Invalid argument provided", "A service method received an argument it cannot process.", false, null),
        new ErrorEntry(400, "INJECTION_DETECTED", "Potentially dangerous input detected", "The request was blocked because it contains SQL injection or XSS patterns.",  false, null),
        new ErrorEntry(401, "UNAUTHORIZED", "Authentication required", "Add a valid Bearer JWT token in the Authorization header.", false, null),
        new ErrorEntry(403, "FORBIDDEN", "Insufficient permissions",  "Your token is valid but does not have the required role or scope.", false, null),
        new ErrorEntry(404, "RESOURCE_NOT_FOUND", "Resource not found", "The resource with the given ID does not exist or has been deleted.", false, null),
        new ErrorEntry(404, "ENDPOINT_NOT_FOUND", "Endpoint not found", "No endpoint exists at this path. Check the URL.", false, null),
        new ErrorEntry(405, "METHOD_NOT_ALLOWED", "HTTP method not allowed", "The endpoint exists but does not support this HTTP verb.", false, null),
        new ErrorEntry(406, "NOT_ACCEPTABLE", "Cannot produce requested media type", "The server cannot produce a response matching the Accept header.", false, null),
        new ErrorEntry(409, "CONFLICT", "Resource already exists", "A resource with the same unique key already exists.", false, null),
        new ErrorEntry(409, "ILLEGAL_STATE", "Operation not permitted in current state", "The resource exists but its current state does not allow this operation.", false, null),
        new ErrorEntry(413, "UPLOAD_TOO_LARGE", "Uploaded file exceeds size limit", "Reduce the file size or contact the administrator to increase the limit.", false, null),
        new ErrorEntry(415, "UNSUPPORTED_MEDIA_TYPE", "Content-Type not supported", "Send the request with Content-Type: application/json.", false, null),
        new ErrorEntry(422, "VALIDATION_FAILED",  "Business rule violation", "The request is syntactically valid but violates a domain rule.",  false, null),
        new ErrorEntry(429, "RATE_LIMIT_EXCEEDED", "Too many requests", "You have exceeded the rate limit. Slow down and retry after the reset window.", true, 60),

        // 5xx Server errors 

        new ErrorEntry(500, "INTERNAL_ERROR", "Unexpected server error",  "An unhandled exception occurred. Use the 'requestId' to find the trace in server logs.", false, null),
        new ErrorEntry(501, "NOT_IMPLEMENTED", "Feature not yet implemented", "This endpoint exists in the API contract but has not been implemented yet.", false, null),
        new ErrorEntry(503, "EXTERNAL_SERVICE_UNAVAILABLE", "Downstream service unavailable", "A dependency is temporarily unavailable. Retry after retryAfterSeconds.", true, 30)
    );

    // Endpoints 

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all error codes", operationId = "listErrors", description = "Returns the complete catalog of every error code this API can return, grouped by HTTP status.")
    public ResponseEntity<ApiResponse<List<ErrorEntry>>> list() {
        return ApiResponse.ok(CATALOG, "Error catalog retrieved");
    }

    @GetMapping("/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a single error entry by its UPPER_SNAKE_CASE code")
    public ResponseEntity<ApiResponse<ErrorEntry>> getByCode(@PathVariable String code) {
        return CATALOG.stream()
            .filter(e -> e.code().equalsIgnoreCase(code))
            .findFirst()
            .map(e -> ApiResponse.ok(e, "Error entry found"))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deliberately triggers a specific HTTP error — for frontend/integration testing.
     *
     * Examples:
     *   GET /api/v1/errors/simulate/400   → validation-like 400
     *   GET /api/v1/errors/simulate/401   → unauthorized
     *   GET /api/v1/errors/simulate/404   → not found
     *   GET /api/v1/errors/simulate/500   → internal error
     */
    @GetMapping("/simulate/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Simulate an HTTP error (dev/test only)",
        description = """
            Deliberately triggers the requested HTTP error so you can test your
            frontend error-handling without needing a real failure.
            Supported codes: 400, 401, 403, 404, 409, 422, 429, 500, 503
            """)
    public ResponseEntity<?> simulate(@PathVariable int status) {
        // Note: 401/403 require Spring Security — add those cases after adding the security module
        switch (status) {
            case 400 -> throw new IllegalArgumentException("Simulated bad request — invalid input");
            case 404 -> throw new ResourceNotFoundException("SimulatedResource", "test-id");
            case 409 -> throw new ConflictException("Simulated conflict — resource already exists");
            case 422 -> throw new BusinessException("SIMULATED_BUSINESS_RULE", "Simulated business rule violation");
            case 429 -> throw new BusinessException("RATE_LIMIT_EXCEEDED", "Simulated rate limit — too many requests", org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
            case 500 -> throw new RuntimeException("Simulated internal server error");
            case 503 -> throw new ExternalServiceException("simulated-service", "Simulated service unavailable");
            default  -> throw new IllegalArgumentException("Unsupported code: " + status + ". Supported: 400, 404, 409, 422, 429, 500, 503 (add 401/403 after adding the security module)");
        }
    }

    // DTO 

	public record ErrorEntry(int httpStatus, 
		String code, 
		String title, 
		String description, 
		boolean retryable,
		Integer retryAfterSeconds) { 
	}
}
