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
 * Response envelope for every SUCCESSFUL endpoint (2xx). Errors are not
 * wrapped: they are RFC 9457 problem-details documents produced by
 * {@link com.fourati.platform.error.GlobalExceptionHandler} (ADR 0005).
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
 * @param <T> type of the payload — use List<X> for paginated results
 */
@Schema(description = "Success envelope. Every 2xx response has this shape; errors are application/problem+json.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(

        @Schema(description = "Always true: errors are not wrapped in this envelope", example = "true")
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

        @Schema(description = "Response payload")
        T data,

        @Schema(description = "Pagination metadata — only present on list endpoints")
        Pagination pagination) {

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

    // ── Factory methods ────────────────────────────────────────────────────────

    /** 200 OK with data and default message. */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ok(data, "Success");
    }

    /** 200 OK with data and custom message. */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(build(200, message, data, null));
    }

    /** 201 Created with data. */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return created(data, "Resource created successfully");
    }

    /** 201 Created with data and custom message. */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(build(201, message, data, null));
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
        return build(200, message, page.getContent(), Pagination.of(page));
    }

    // ── Core builder ───────────────────────────────────────────────────────────

    private static <T> ApiResponse<T> build(int status, String message, T data, Pagination pagination) {
        return new ApiResponse<>(
            true, status, message,
            Instant.now().toString(),
            MDC.get("correlationId"),
            data, pagination
        );
    }
}
