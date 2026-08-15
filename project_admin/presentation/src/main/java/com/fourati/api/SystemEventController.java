package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.SystemEventResponse;
import com.fourati.service.SystemEventService;
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
 * Read-only API for system/application events captured for observability
 * purposes. Entries are written internally by the application; no
 * create/update/delete endpoints are exposed here.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/system-events")
@Tag(name = "System Events", description = "Read-only access to system/application event entries")
@PreAuthorize("hasRole('ADMIN')")
public class SystemEventController {

    private final SystemEventService systemEventService;

    @GetMapping
    @Operation(summary = "List system events, optionally filtered by event type or severity")
    public ResponseEntity<ApiResponse<List<SystemEventResponse>>> list(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String severity,
            Pageable pageable) {
        Page<SystemEventResponse> page;
        if (eventType != null) {
            page = systemEventService.findByEventType(eventType, pageable);
        } else if (severity != null) {
            page = systemEventService.findBySeverity(severity, pageable);
        } else {
            page = systemEventService.findAll(pageable);
        }
        return ApiResponse.paged(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single system event by id")
    public ResponseEntity<ApiResponse<SystemEventResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(systemEventService.findById(id));
    }
}
