package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.ErrorLogResponse;
import com.fourati.service.ErrorLogService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Read-only API for error log entries captured for observability purposes.
 * Entries are written internally by the application; no create/update/delete
 * endpoints are exposed here.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/error-logs")
@Tag(name = "Error Logs", description = "Read-only access to application error log entries")
@PreAuthorize("hasRole('ADMIN')")
public class ErrorLogController {

    private final ErrorLogService errorLogService;

    @GetMapping
    @Operation(summary = "List error log entries, optionally filtered by severity or source")
    public ResponseEntity<ApiResponse<List<ErrorLogResponse>>> list(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String source,
            Pageable pageable) {
        Page<ErrorLogResponse> page;
        if (severity != null) {
            page = errorLogService.findBySeverity(severity, pageable);
        } else if (source != null) {
            page = errorLogService.findBySource(source, pageable);
        } else {
            page = errorLogService.findAll(pageable);
        }
        return ApiResponse.paged(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single error log entry by id")
    public ResponseEntity<ApiResponse<ErrorLogResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(errorLogService.findById(id));
    }
}
