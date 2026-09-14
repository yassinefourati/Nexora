package com.fourati.platform.error;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.MDC;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
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
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.fourati.platform.error.Problems.Violation;
import com.fourati.platform.export.ExportException;
import com.fourati.platform.util.NumberUtils;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Central exception handler. Every error is an RFC 9457 problem-details
 * document built by {@link Problems} (ADR 0005):
 * <pre>
 * {
 *   "type":          "https://errors.example.com/resource-not-found",
 *   "title":         "Resource not found",
 *   "status":        404,
 *   "detail":        "User not found with id: abc",
 *   "instance":      "/api/v1/users/abc",
 *   "code":          "RESOURCE_NOT_FOUND",
 *   "retryable":     false,
 *   "timestamp":     "2026-09-13T12:00:00Z",
 *   "correlationId": "d993afa6-...",
 *   "violations":    [ { "field": "name", "code": "NOT_BLANK", "message": "must not be blank" } ]
 * }
 * </pre>
 *
 * Logging: 5xx at ERROR with the stack trace, 4xx at WARN, routing noise at DEBUG.
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final Problems problems;

    // ═══════════════════════════════════════════════════════════════════
    // SECURITY
    // ═══════════════════════════════════════════════════════════════════

    /**
     * A {@code @PreAuthorize} denial inside a controller surfaces as
     * {@link org.springframework.security.authorization.AuthorizationDeniedException}, an
     * {@link AccessDeniedException}, which Spring MVC routes to this advice before Spring
     * Security's ExceptionTranslationFilter ever sees it. Without this handler the catch-all
     * below turned every 403 into a 500 (caught by ErrorContractTest and the security ITs).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, WebRequest req) {
        log.warn("Access denied on {}", path(req));
        return problem(ErrorCode.FORBIDDEN, "You do not have permission to perform this action.", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex, WebRequest req) {
        log.warn("Authentication failed on {}: {}", path(req), ex.getMessage());
        return problem(ErrorCode.UNAUTHORIZED, "Authentication is required to perform this action.", req);
    }

    // ═══════════════════════════════════════════════════════════════════
    // DOMAIN
    // ═══════════════════════════════════════════════════════════════════

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex, WebRequest req) {
        log.warn("Not found: {}", ex.getMessage());
        return problem(ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLock(OptimisticLockingFailureException ex, WebRequest req) {
        log.warn("Optimistic lock conflict on {}", path(req));
        return problem(ErrorCode.CONCURRENT_MODIFICATION,
            "The resource was modified by someone else. Reload and retry.", req);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex, WebRequest req) {
        log.warn("Conflict: {}", ex.getMessage());
        return problem(ErrorCode.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex, WebRequest req) {
        log.warn("Business rule [{}]: {}", ex.getErrorCode(), ex.getMessage());
        ErrorCode known = lookup(ex.getErrorCode());
        ProblemDetail body = known != null
            ? problems.of(ex.getStatus(), known.code(), known.title(), ex.getMessage(), known.retryable(), path(req), null)
            : problems.of(ex.getStatus(), ex.getErrorCode(), null, ex.getMessage(), false, path(req), null);
        return respond(body);
    }

    @ExceptionHandler(ExportException.class)
    public ResponseEntity<ProblemDetail> handleExport(ExportException ex, WebRequest req) {
        log.warn("Export failed on {}: {} — cause: {}", path(req), ex.getMessage(),
            ex.getCause() != null ? ex.getCause().getMessage() : "unknown");
        return problem(ErrorCode.EXPORT_FAILED,
            "The export could not be generated. Reference: " + MDC.get("correlationId"), req);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ProblemDetail> handleExternalService(ExternalServiceException ex, WebRequest req) {
        log.warn("External service '{}' failed: {}", ex.getServiceName(), ex.getMessage());
        return problem(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
            "A downstream service is temporarily unavailable. Please retry in a few moments.", req);
    }

    // ═══════════════════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════════════════

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleBodyValidation(MethodArgumentNotValidException ex, WebRequest req) {
        List<Violation> violations = ex.getBindingResult().getAllErrors().stream()
            .map(e -> {
                if (e instanceof FieldError fe) {
                    return new Violation(
                        fe.getField(),
                        constraintCode(fe.getCodes()),
                        fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        isSensitive(fe.getField()) ? null : fe.getRejectedValue()
                    );
                }
                return new Violation(
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
    public ResponseEntity<ProblemDetail> handleMethodValidation(HandlerMethodValidationException ex, WebRequest req) {
        List<Violation> violations = ex.getAllErrors().stream()
            .map(e -> new Violation(null, "INVALID",
                e.getDefaultMessage() != null ? e.getDefaultMessage() : "invalid", null))
            .toList();
        log.warn("Method parameter validation: {} violation(s)", violations.size());
        return validationError(req, violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex, WebRequest req) {
        List<Violation> violations = ex.getConstraintViolations().stream()
            .map(cv -> new Violation(
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
    public ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex, WebRequest req) {
        log.warn("Malformed body: {}", ex.getMessage());
        return problem(ErrorCode.MALFORMED_REQUEST_BODY,
            "The request body could not be parsed. Verify JSON syntax and field types.", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest req) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String msg = "Parameter '%s' must be of type %s but received: '%s'"
            .formatted(ex.getName(), expected, ex.getValue());
        log.warn("Type mismatch: {}", msg);
        return respond(problems.of(ErrorCode.TYPE_MISMATCH, msg, path(req),
            List.of(new Violation(ex.getName(), "TYPE_MISMATCH", msg, ex.getValue()))));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParam(MissingServletRequestParameterException ex, WebRequest req) {
        String msg = "Required parameter '%s' (%s) is missing"
            .formatted(ex.getParameterName(), ex.getParameterType());
        log.warn(msg);
        return respond(problems.of(ErrorCode.MISSING_PARAMETER, msg, path(req),
            List.of(new Violation(ex.getParameterName(), "REQUIRED", msg, null))));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetail> handleMissingHeader(MissingRequestHeaderException ex, WebRequest req) {
        String msg = "Required header '%s' is missing".formatted(ex.getHeaderName());
        log.warn(msg);
        return problem(ErrorCode.MISSING_HEADER, msg, req);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ProblemDetail> handleMissingPath(MissingPathVariableException ex, WebRequest req) {
        log.warn("Missing path variable: {}", ex.getVariableName());
        return problem(ErrorCode.MISSING_PATH_VARIABLE,
            "Path variable '%s' is missing from the URL template".formatted(ex.getVariableName()), req);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ProblemDetail> handleMultipart(MultipartException ex, WebRequest req) {
        log.warn("Multipart error: {}", ex.getMessage());
        return problem(ErrorCode.MULTIPART_ERROR, "The multipart request could not be processed.", req);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUpload(MaxUploadSizeExceededException ex, WebRequest req) {
        long max = ex.getMaxUploadSize();
        String limit = max > 0 ? NumberUtils.formatBytes(max) : "configured limit";
        log.warn("Upload too large: max={}", limit);
        return problem(ErrorCode.CONTENT_TOO_LARGE,
            "The uploaded file exceeds the maximum allowed size of " + limit, req);
    }

    // ═══════════════════════════════════════════════════════════════════
    // ROUTING
    // ═══════════════════════════════════════════════════════════════════

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoHandler(NoResourceFoundException ex, WebRequest req) {
        log.debug("No handler: {}", path(req));
        return problem(ErrorCode.ENDPOINT_NOT_FOUND, "No endpoint exists at this path.", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, WebRequest req) {
        String msg = "HTTP %s is not allowed here. Allowed: %s".formatted(ex.getMethod(), ex.getSupportedHttpMethods());
        log.debug(msg);
        return problem(ErrorCode.METHOD_NOT_ALLOWED, msg, req);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedType(HttpMediaTypeNotSupportedException ex, WebRequest req) {
        String msg = "Content-Type '%s' is not supported. Supported: %s"
            .formatted(ex.getContentType(), ex.getSupportedMediaTypes());
        log.warn(msg);
        return problem(ErrorCode.UNSUPPORTED_MEDIA_TYPE, msg, req);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ProblemDetail> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex, WebRequest req) {
        log.warn("Not acceptable: {}", ex.getSupportedMediaTypes());
        return problem(ErrorCode.NOT_ACCEPTABLE, "Cannot produce a response in the requested format.", req);
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ProblemDetail> handleNotWritable(HttpMessageNotWritableException ex, WebRequest req) {
        log.error("Serialization failed: {}", ex.getMessage(), ex);
        return problem(ErrorCode.SERIALIZATION_ERROR, "The server could not serialize the response.", req);
    }

    // ═══════════════════════════════════════════════════════════════════
    // PROGRAMMING ERRORS
    // ═══════════════════════════════════════════════════════════════════

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException ex, WebRequest req) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        String detail = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        if (status.is5xxServerError()) log.error("ResponseStatusException [{}]: {}", status, detail, ex);
        else log.warn("ResponseStatusException [{}]: {}", status, detail);
        return respond(problems.of(status, status.name(), null, detail, false, path(req), null));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ProblemDetail> handleUnsupported(UnsupportedOperationException ex, WebRequest req) {
        log.error("Not implemented on {}", path(req), ex);
        return problem(ErrorCode.NOT_IMPLEMENTED,
            "This feature is not yet implemented. Reference: " + MDC.get("correlationId"), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex, WebRequest req) {
        log.warn("IllegalArgument on {}: {}", path(req), ex.getMessage());
        return problem(ErrorCode.INVALID_ARGUMENT,
            ex.getMessage() != null ? ex.getMessage() : "Invalid input provided", req);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex, WebRequest req) {
        log.warn("IllegalState on {}: {}", path(req), ex.getMessage());
        return problem(ErrorCode.ILLEGAL_STATE,
            ex.getMessage() != null ? ex.getMessage() : "Operation not permitted in the current state", req);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchElement(NoSuchElementException ex, WebRequest req) {
        log.warn("NoSuchElement on {}", path(req));
        return problem(ErrorCode.RESOURCE_NOT_FOUND, "The requested resource does not exist.", req);
    }

    // ═══════════════════════════════════════════════════════════════════
    // CATCH-ALL
    // ═══════════════════════════════════════════════════════════════════

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex, WebRequest req) {
        log.error("Unhandled exception on {}: {}", path(req), ex.getMessage(), ex);
        return problem(ErrorCode.INTERNAL_ERROR,
            "An unexpected error occurred. Reference: " + MDC.get("correlationId"), req);
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════

    private ResponseEntity<ProblemDetail> validationError(WebRequest req, List<Violation> violations) {
        return respond(problems.of(ErrorCode.VALIDATION_FAILED,
            "Request validation failed. See 'violations' for field-level details.", path(req), violations));
    }

    private ResponseEntity<ProblemDetail> problem(ErrorCode code, String detail, WebRequest req) {
        return respond(problems.of(code, detail, path(req)));
    }

    private static ResponseEntity<ProblemDetail> respond(ProblemDetail body) {
        return ResponseEntity.status(HttpStatusCode.valueOf(body.getStatus()))
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(body);
    }

    private static ErrorCode lookup(String code) {
        if (code == null) return null;
        try {
            return ErrorCode.valueOf(code);
        } catch (IllegalArgumentException notInCatalog) {
            return null;
        }
    }

    private String path(WebRequest req) {
        String desc = req.getDescription(false);
        return desc.startsWith("uri=") ? desc.substring(4) : desc;
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
