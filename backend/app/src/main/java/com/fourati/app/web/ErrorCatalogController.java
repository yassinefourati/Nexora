package com.fourati.app.web;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourati.shared.api.ApiConstants;
import com.fourati.platform.error.BusinessRuleException;
import com.fourati.platform.error.ErrorCode;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ExternalServiceException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.platform.web.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Error catalog, generated from {@link ErrorCode} so it can never disagree
 * with what the API actually returns (ADR 0005).
 *
 * GET  /api/v1/errors → full catalog
 * GET  /api/v1/errors/{code} → single entry
 * GET  /api/v1/errors/simulate/{httpStatus} → deliberately triggers that HTTP error (dev/test only)
 */
@Profile("!prod")
@RestController
@RequestMapping(ApiConstants.VERSION + "/errors")
@Tag(name = "Error Catalog", description = """
    Complete catalog of every error code the API can return, as RFC 9457 problem details.
    Use `/simulate/{status}` to test your frontend error-handling without needing real failures.
    """)
public class ErrorCatalogController {

    private static final List<ErrorEntry> CATALOG = Arrays.stream(ErrorCode.values())
        .map(ErrorEntry::of)
        .toList();

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all error codes", operationId = "listErrors",
        description = "Returns the complete catalog of every error code this API can return, with the HTTP status each maps to.")
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
            .orElseThrow(() -> new ResourceNotFoundException("ErrorCode", code));
    }

    /**
     * Deliberately triggers a specific HTTP error — for frontend/integration testing.
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
        switch (status) {
            case 400 -> throw new IllegalArgumentException("Simulated bad request — invalid input");
            case 401 -> throw new BadCredentialsException("Simulated authentication failure");
            case 403 -> throw new AuthorizationDeniedException("Simulated access denied");
            case 404 -> throw new ResourceNotFoundException("SimulatedResource", "test-id");
            case 409 -> throw new ConflictException("Simulated conflict — resource already exists");
            case 422 -> throw new BusinessRuleException(ErrorCode.BUSINESS_RULE_VIOLATION, "Simulated business rule violation");
            case 429 -> throw new BusinessRuleException(ErrorCode.RATE_LIMIT_EXCEEDED, "Simulated rate limit — too many requests");
            case 500 -> throw new RuntimeException("Simulated internal server error");
            case 503 -> throw new ExternalServiceException("simulated-service", "Simulated service unavailable");
            default  -> throw new IllegalArgumentException("Unsupported code: " + status + ". Supported: 400, 401, 403, 404, 409, 422, 429, 500, 503");
        }
    }

    /** One catalog entry, derived from an {@link ErrorCode}. */
    public record ErrorEntry(int httpStatus, String code, String title, String description, boolean retryable) {

        static ErrorEntry of(ErrorCode code) {
            return new ErrorEntry(code.status().value(), code.code(), code.title(), code.description(), code.retryable());
        }
    }
}
