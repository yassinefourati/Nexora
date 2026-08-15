package com.fourati.platform.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

/**
 * Standard response envelope for ALL API endpoints — success and error alike.
 *
 * Success (single):
 * {
 *   "success":       true,
 *   "status":        200,
 *   "message":       "Item retrieved",
 *   "timestamp":     "2026-06-04T12:00:00.000Z",
 *   "correlationId": "d993afa6-...",
 *   "data": { "id": "...", "name": "Widget A" }
 * }
 *
 * Success (paginated):
 * {
 *   "success": true, "status": 200, "message": "Items retrieved",
 *   "data": [...],
 *   "pagination": { "page": 0, "size": 20, "totalElements": 150, "totalPages": 8,
 *                   "first": true, "last": false }
 * }
 *
 * Error (from GlobalExceptionHandler):
 * {
 *   "success":       false,
 *   "status":        404,
 *   "message":       "Item not found with id: abc",
 *   "timestamp":     "...",
 *   "correlationId": "...",
 *   "error": {
 *     "code":      "RESOURCE_NOT_FOUND",
 *     "type":      "https://errors.example.com/errors/resource-not-found",
 *     "instance":  "/api/v1/items/abc",
 *     "retryable": false
 *   }
 * }
 *
 * Validation error (400):
 * {
 *   "success": false, "status": 400,
 *   "error": {
 *     "code": "VALIDATION_FAILED",
 *     "violations": [{ "field": "name", "code": "NOT_BLANK", "message": "must not be blank" }]
 *   }
 * }
 *
 * @param <T> type of the payload — use List<X> for paginated results, Void for errors/204
 */
@Schema(description = "Standard API response envelope. Every endpoint — success or error — returns this shape.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(

        @Schema(description = "true on success, false on error", example = "true")
        boolean success,

        @Schema(description = "HTTP status code", example = "200")
        int status,

        @Schema(description = "Human-readable result message", example = "Item retrieved")
        String message,

        @Schema(description = "ISO-8601 UTC timestamp", example = "2026-06-04T12:00:00.000Z")
        String timestamp,

        @Schema(description = "Correlation ID from X-Correlation-ID header — use to trace logs",
                example = "d993afa6-5486-4221-8085-dbcf19fce65f")
        String correlationId,

        @Schema(description = "Response payload — null on errors")
        T data,

        @Schema(description = "Pagination metadata — only present on list endpoints")
        Pagination pagination,

        @Schema(description = "Error details — only present on failure responses")
        ErrorDetail error) {

    // ── Nested types ───────────────────────────────────────────────────────────

    @Schema(description = "Pagination metadata included on list responses")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Pagination(
        @Schema(description = "Zero-based current page number", example = "0") int page,
        @Schema(description = "Items per page", example = "20") int size,
        @Schema(description = "Total items across all pages", example = "150") long totalElements,
        @Schema(description = "Total pages", example = "8") int totalPages,
        @Schema(description = "True if first page", example = "true") boolean first,
        @Schema(description = "True if last page", example = "false") boolean last
    ) {
        public static Pagination of(Page<?> page) {
            return new Pagination(
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(),
                page.isFirst(), page.isLast()
            );
        }
    }

    /**
     * Error detail block — present only on failure responses.
     * Replaces the old separate {@code ErrorResponse} type: every response now
     * uses the same {@code ApiResponse} envelope regardless of success or failure.
     */
    @Schema(description = "Error detail block — present only on failure responses")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorDetail(
        @Schema(description = "UPPER_SNAKE_CASE error code", example = "RESOURCE_NOT_FOUND")
        String code,

        @Schema(description = "URI identifying the error category",
                example = "https://errors.example.com/errors/resource-not-found")
        String type,

        @Schema(description = "Request path that triggered the error", example = "/api/v1/items/abc")
        String instance,

        @Schema(description = "true = client should retry; false = fix the request first",
                example = "false")
        boolean retryable,

        @Schema(description = "Field-level violations — only present on VALIDATION_FAILED")
        List<Violation> violations
    ) {}

    /**
     * A single field-level constraint violation (present on 400 VALIDATION_FAILED errors).
     */
    @Schema(description = "A single field-level constraint violation")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Violation(
        @Schema(description = "Dot-notation field path", example = "name", nullable = true)
        String field,

        @Schema(description = "Constraint code", example = "NOT_BLANK")
        String code,

        @Schema(description = "Constraint violation message", example = "must not be blank")
        String message,

        @Schema(description = "Rejected value — null when sensitive", nullable = true)
        Object rejectedValue
    ) {}

    // ── Factory methods — success ──────────────────────────────────────────────

    /** 200 OK with data and default message. */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ok(data, "Success");
    }

    /** 200 OK with data and custom message. */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(build(true, 200, message, data, null, null));
    }

    /** 201 Created with data. */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return created(data, "Resource created successfully");
    }

    /** 201 Created with data and custom message. */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(build(true, 201, message, data, null, null));
    }

    /** 204 No Content (delete, void operations). */
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /** 200 OK — paginated list. */
    public static <T> ResponseEntity<ApiResponse<List<T>>> paged(Page<T> page) {
        return paged(page, "Success");
    }

    /** 200 OK — paginated list with custom message. */
    public static <T> ResponseEntity<ApiResponse<List<T>>> paged(Page<T> page, String message) {
        return ResponseEntity.ok(pagedBody(page, message));
    }

    /**
     * Builds the paginated body without wrapping in ResponseEntity —
     * use when you need to set headers manually.
     */
    public static <T> ApiResponse<List<T>> pagedBody(Page<T> page, String message) {
        return build(true, 200, message, page.getContent(), Pagination.of(page), null);
    }

    // ── Factory method — error ─────────────────────────────────────────────────

    /**
     * Builds an error body ({@code success: false}).
     * Used by {@link com.fourati.platform.error.GlobalExceptionHandler} —
     * not typically called from controllers directly.
     */
    public static <T> ApiResponse<T> error(int status, String message, ErrorDetail errorDetail) {
        return build(false, status, message, null, null, errorDetail);
    }

    // ── Core builder ───────────────────────────────────────────────────────────

    private static <T> ApiResponse<T> build(boolean success, int status, String message,
            T data, Pagination pagination, ErrorDetail error) {
        return new ApiResponse<>(
            success, status, message,
            Instant.now().toString(),
            MDC.get("correlationId"),
            data, pagination, error
        );
    }
}
