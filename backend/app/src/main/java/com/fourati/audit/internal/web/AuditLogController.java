package com.fourati.audit.internal.web;

import com.fourati.shared.api.ApiConstants;
import com.fourati.audit.AuditLogResponse;
import com.fourati.audit.internal.application.AuditLogService;
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
import com.fourati.platform.export.ExportFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

/**
 * Read-only API for the audit trail. Entries are written internally by the
 * application whenever a create/update/delete action is performed on a
 * tracked entity; no create/update/delete endpoints are exposed here.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/audit-logs")
@Tag(name = "Audit Logs", description = "Read-only access to the system audit trail")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "List audit log entries, optionally filtered by user or by target entity")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> list(

            @RequestParam(required = false) String q,            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            Pageable pageable) {
        if (entityType != null && entityId != null) {
            return ApiResponse.ok(auditLogService.findByEntity(entityType, entityId));
        }
        Page<AuditLogResponse> page = userId != null
                ? auditLogService.findByUserId(userId, pageable)
                : auditLogService.search(q, pageable);
        return ApiResponse.paged(page);
    }

    @GetMapping("/export")
    @Operation(summary = "Export the newest audit entries as CSV, PDF or JSON (capped at app.export.max-rows)")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "CSV") ExportFormat format) {
        byte[] body = auditLogService.export(userId, format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log." + format.getExtension() + "\"")
                .contentType(MediaType.parseMediaType(format.getMediaType()))
                .body(body);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single audit log entry by id")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(auditLogService.findById(id));
    }
}
