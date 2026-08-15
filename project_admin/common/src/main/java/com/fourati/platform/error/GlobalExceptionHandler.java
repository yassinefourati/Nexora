package com.fourati.platform.error;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.fourati.platform.export.ExportException;
import com.fourati.platform.util.NumberUtils;
import com.fourati.platform.properties.CommonProperties;
import com.fourati.platform.web.ApiResponse;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Central exception handler — returns {@link ApiResponse} with {@code success: false}.
 *
 * Every error uses the same envelope as successful responses:
 * <pre>
 * {
 *   "success":       false,
 *   "status":        404,
 *   "message":       "Item not found with id: abc",
 *   "timestamp":     "...",
 *   "correlationId": "...",
 *   "error": {
 *     "code":     "RESOURCE_NOT_FOUND",
 *     "type":     "https://errors.example.com/errors/resource-not-found",
 *     "instance": "/api/v1/items/abc"
 *   }
 * }
 * </pre>
 *
 * Logging:
 *   5xx → ERROR with full stack trace
 *   4xx → WARN (no stack trace)
 *   routing → DEBUG
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final CommonProperties commonProperties;

    // ═══════════════════════════════════════════════════════════════════
    // DOMAIN
    // ═══════════════════════════════════════════════════════════════════

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex, WebRequest req) {
        log.warn("Not found: {}", ex.getMessage());
        return err(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), false, req, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException ex, WebRequest req) {
        log.warn("Conflict: {}", ex.getMessage());
        return err(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), false, req, null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex, WebRequest req) {
        log.warn("Business rule [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return err(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), false, req, null);
    }

    @ExceptionHandler(ExportException.class)
    public ResponseEntity<ApiResponse<Void>> handleExport(ExportException ex, WebRequest req) {
        log.warn("Export failed on {}: {} — cause: {}", path(req), ex.getMessage(),
            ex.getCause() != null ? ex.getCause().getMessage() : "unknown");
        return err(HttpStatus.INTERNAL_SERVER_ERROR, "EXPORT_FAILED",
            "The export could not be generated. Reference: " + MDC.get("correlationId"), false, req, null);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleExternalService(ExternalServiceException ex, WebRequest req) {
        log.warn("External service '{}' failed: {}", ex.getServiceName(), ex.getMessage());
        return err(HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_SERVICE_UNAVAILABLE",
            "A downstream service is temporarily unavailable. Please retry in a few moments.", true, req, null);
    }

    // ═══════════════════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════════════════

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBodyValidation(MethodArgumentNotValidException ex, WebRequest req) {
        List<ApiResponse.Violation> violations = ex.getBindingResult().getAllErrors().stream()
            .map(e -> {
                if (e instanceof FieldError fe) {
                    return new ApiResponse.Violation(
                        fe.getField(),
                        constraintCode(fe.getCodes()),
                        fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        isSensitive(fe.getField()) ? null : fe.getRejectedValue()
                    );
                }
                return new ApiResponse.Violation(
                    e.getObjectName(), "GLOBAL",
                    e.getDefaultMessage() != null ? e.getDefaultMessage() : "invalid",
                    null
                );
            })
            .toList();

        log.warn("Validation failed on '{}': {} violation(s)", ex.getObjectName(), violations.size());
        return validationError(req, violations);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodValidation(HandlerMethodValidationException ex, WebRequest req) {
        List<ApiResponse.Violation> violations = ex.getAllErrors().stream()
            .map(e -> new ApiResponse.Violation(null, "INVALID",
                e.getDefaultMessage() != null ? e.getDefaultMessage() : "invalid", null))
            .toList();
        log.warn("Method parameter validation: {} violation(s)", violations.size());
        return validationError(req, violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex, WebRequest req) {
        List<ApiResponse.Violation> violations = ex.getConstraintViolations().stream()
            .map(cv -> new ApiResponse.Violation(
                lastNode(cv.getPropertyPath().toString()),
                cv.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName().toUpperCase(),
                cv.getMessage(),
                isSensitive(lastNode(cv.getPropertyPath().toString())) ? null : cv.getInvalidValue()
            ))
            .toList();
        log.warn("Constraint violations: {}", violations.size());
        return validationError(req, violations);
    }

    // ═══════════════════════════════════════════════════════════════════
    // BAD REQUEST
    // ═══════════════════════════════════════════════════════════════════

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex, WebRequest req) {
        log.warn("Malformed body: {}", ex.getMessage());
        return err(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST_BODY",
            "The request body could not be parsed. Verify JSON syntax and field types.", false, req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest req) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String msg = "Parameter '%s' must be of type %s but received: '%s'"
            .formatted(ex.getName(), expected, ex.getValue());
        log.warn("Type mismatch: {}", msg);
        List<ApiResponse.Violation> v = List.of(
            new ApiResponse.Violation(ex.getName(), "TYPE_MISMATCH", msg, ex.getValue()));
        return err(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", msg, false, req, v);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex, WebRequest req) {
        String msg = "Required parameter '%s' (%s) is missing"
            .formatted(ex.getParameterName(), ex.getParameterType());
        log.warn(msg);
        List<ApiResponse.Violation> v = List.of(
            new ApiResponse.Violation(ex.getParameterName(), "REQUIRED", msg, null));
        return err(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", msg, false, req, v);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex, WebRequest req) {
        String msg = "Required header '%s' is missing".formatted(ex.getHeaderName());
        log.warn(msg);
        return err(HttpStatus.BAD_REQUEST, "MISSING_HEADER", msg, false, req, null);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingPath(MissingPathVariableException ex, WebRequest req) {
        log.warn("Missing path variable: {}", ex.getVariableName());
        return err(HttpStatus.BAD_REQUEST, "MISSING_PATH_VARIABLE",
            "Path variable '%s' is missing from the URL template".formatted(ex.getVariableName()),
            false, req, null);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipart(MultipartException ex, WebRequest req) {
        log.warn("Multipart error: {}", ex.getMessage());
        return err(HttpStatus.BAD_REQUEST, "MULTIPART_ERROR",
            "The multipart request could not be processed.", false, req, null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUpload(MaxUploadSizeExceededException ex, WebRequest req) {
        long max = ex.getMaxUploadSize();
        String limit = max > 0 ? NumberUtils.formatBytes(max) : "configured limit";
        log.warn("Upload too large: max={}", limit);
        return err(HttpStatus.CONTENT_TOO_LARGE, "CONTENT_TOO_LARGE",
            "The uploaded file exceeds the maximum allowed size of " + limit, false, req, null);
    }

    // ═══════════════════════════════════════════════════════════════════
    // ROUTING
    // ═══════════════════════════════════════════════════════════════════

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(NoResourceFoundException ex, WebRequest req) {
        log.debug("No handler: {}", path(req));
        return err(HttpStatus.NOT_FOUND, "ENDPOINT_NOT_FOUND", "No endpoint exists at this path.", false, req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, WebRequest req) {
        String msg = "HTTP %s is not allowed here. Allowed: %s".formatted(ex.getMethod(), ex.getSupportedHttpMethods());
        log.debug(msg);
        return err(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", msg, false, req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedType(HttpMediaTypeNotSupportedException ex, WebRequest req) {
        String msg = "Content-Type '%s' is not supported. Supported: %s"
            .formatted(ex.getContentType(), ex.getSupportedMediaTypes());
        log.warn(msg);
        return err(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", msg, false, req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex, WebRequest req) {
        log.warn("Not acceptable: {}", ex.getSupportedMediaTypes());
        return err(HttpStatus.NOT_ACCEPTABLE, "NOT_ACCEPTABLE",
            "Cannot produce a response in the requested format.", false, req, null);
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotWritable(HttpMessageNotWritableException ex, WebRequest req) {
        log.error("Serialization failed: {}", ex.getMessage(), ex);
        return err(HttpStatus.INTERNAL_SERVER_ERROR, "SERIALIZATION_ERROR",
            "The server could not serialize the response.", false, req, null);
    }

    // ═══════════════════════════════════════════════════════════════════
    // PROGRAMMING ERRORS
    // ═══════════════════════════════════════════════════════════════════

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException ex, WebRequest req) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        String detail = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        if (status.is5xxServerError()) log.error("ResponseStatusException [{}]: {}", status, detail, ex);
        else log.warn("ResponseStatusException [{}]: {}", status, detail);
        return err(status, status.name().replace(' ', '_'), detail, false, req, null);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupported(UnsupportedOperationException ex, WebRequest req) {
        log.error("Not implemented on {}", path(req), ex);
        return err(HttpStatus.NOT_IMPLEMENTED, "NOT_IMPLEMENTED",
            "This feature is not yet implemented. Reference: " + MDC.get("correlationId"), false, req, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex, WebRequest req) {
        log.warn("IllegalArgument on {}: {}", path(req), ex.getMessage());
        return err(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT",
            ex.getMessage() != null ? ex.getMessage() : "Invalid input provided", false, req, null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex, WebRequest req) {
        log.warn("IllegalState on {}: {}", path(req), ex.getMessage());
        return err(HttpStatus.CONFLICT, "ILLEGAL_STATE",
            ex.getMessage() != null ? ex.getMessage() : "Operation not permitted in the current state", false, req, null);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchElement(NoSuchElementException ex, WebRequest req) {
        log.warn("NoSuchElement on {}", path(req));
        return err(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "The requested resource does not exist.", false, req, null);
    }

    // ═══════════════════════════════════════════════════════════════════
    // CATCH-ALL
    // ═══════════════════════════════════════════════════════════════════

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, WebRequest req) {
        log.error("Unhandled exception on {}: {}", path(req), ex.getMessage(), ex);
        return err(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
            "An unexpected error occurred. Reference: " + MDC.get("correlationId"), false, req, null);
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════

    private ResponseEntity<ApiResponse<Void>> validationError(WebRequest req, List<ApiResponse.Violation> violations) {
        return err(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
            "Request validation failed. See 'violations' for field-level details.", false, req, violations);
    }

    private ResponseEntity<ApiResponse<Void>> err(HttpStatusCode statusCode, String code, String message,
            boolean retryable, WebRequest req, List<ApiResponse.Violation> violations) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

        String baseUrl = commonProperties.getError().getBaseUrl();
        String typeUrl = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
            + code.toLowerCase().replace('_', '-');

        ApiResponse.ErrorDetail errorDetail = new ApiResponse.ErrorDetail(
            code, typeUrl, path(req), retryable, violations
        );

        ApiResponse<Void> body = ApiResponse.error(status.value(), message, errorDetail);
        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(body);
    }

    private String path(WebRequest req) {
        String desc = req.getDescription(false);
        return desc.startsWith("uri=") ? desc.substring(4) : desc;
    }

    private String method(WebRequest req) {
        if (req instanceof ServletWebRequest swr) return swr.getRequest().getMethod();
        return null;
    }

    private String lastNode(String path) {
        int dot = path.lastIndexOf('.');
        return dot >= 0 ? path.substring(dot + 1) : path;
    }

    private String constraintCode(String[] codes) {
        if (codes == null || codes.length == 0) return "INVALID";
        String raw = codes.length > 1 ? codes[codes.length - 2] : codes[0];
        return raw.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }

    /** Masks rejected values for fields that may contain sensitive data. */
    private boolean isSensitive(String field) {
        if (field == null) return false;
        String f = field.toLowerCase();
        return f.contains("password") || f.contains("secret") || f.contains("token")
            || f.contains("key") || f.contains("credit") || f.contains("card");
    }
}
