package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.AuthLogResponse;
import com.fourati.service.AuthLogService;
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
 * Read-only API for authentication event logs. Entries are written internally
 * by the application; no create/update/delete endpoints are exposed here.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/auth-logs")
@Tag(name = "Auth Logs", description = "Read-only access to authentication event logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuthLogController {

    private final AuthLogService authLogService;

    @GetMapping
    @Operation(summary = "List auth log entries, optionally filtered by user or event type")
    public ResponseEntity<ApiResponse<List<AuthLogResponse>>> list(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String eventType,
            Pageable pageable) {
        Page<AuthLogResponse> page;
        if (userId != null) {
            page = authLogService.findByUserId(userId, pageable);
        } else if (eventType != null) {
            page = authLogService.findByEventType(eventType, pageable);
        } else {
            page = authLogService.findAll(pageable);
        }
        return ApiResponse.paged(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single auth log entry by id")
    public ResponseEntity<ApiResponse<AuthLogResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(authLogService.findById(id));
    }
}
