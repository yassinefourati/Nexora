package com.fourati.audit.internal.application;

import com.fourati.shared.persistence.TextSearch;

import com.fourati.audit.internal.domain.AuditLog;
import com.fourati.audit.CreateAuditLogRequest;
import com.fourati.audit.AuditLogResponse;
import com.fourati.audit.internal.persistence.AuditLogRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.platform.export.ExportColumn;
import com.fourati.platform.export.ExportFormat;
import com.fourati.platform.export.ExportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for audit trail entries.
 *
 * audit_logs is append-only/immutable from the API's perspective: entries are
 * written internally by the application (via {@link #record(CreateAuditLogRequest)})
 * whenever a create/update/delete action is performed on a tracked entity. No
 * public update/delete operations are exposed.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;
    private final ExportService exportService;

    @Value("${app.export.max-rows:10000}")
    private int exportMaxRows;

    private static final List<ExportColumn<AuditLogResponse>> EXPORT_COLUMNS = List.of(
            new ExportColumn<>("id", AuditLogResponse::id),
            new ExportColumn<>("createdAt", AuditLogResponse::createdAt),
            new ExportColumn<>("userId", AuditLogResponse::userId),
            new ExportColumn<>("action", AuditLogResponse::action),
            new ExportColumn<>("entityType", AuditLogResponse::entityType),
            new ExportColumn<>("entityId", AuditLogResponse::entityId),
            new ExportColumn<>("ipAddress", AuditLogResponse::ipAddress));

    /**
     * Exports the most recent audit entries (optionally for one actor) in the
     * requested format. Capped at {@code app.export.max-rows} newest rows so a
     * large trail cannot exhaust the heap; narrow by user or use the paged
     * list endpoint for anything bigger.
     */
    @Transactional(readOnly = true)
    public byte[] export(UUID userId, ExportFormat format) {
        Pageable newest = PageRequest.of(0, exportMaxRows, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> page = userId != null
                ? auditLogRepository.findByUserId(userId, newest)
                : auditLogRepository.findAll(newest);
        List<AuditLogResponse> rows = page.map(auditLogMapper::toResponse).getContent();
        return exportService.export(rows, EXPORT_COLUMNS, format, "Audit log");
    }

    @Audited(action = "CREATE", description = "Recorded a new audit log entry")
    public AuditLogResponse record(CreateAuditLogRequest request) {
        AuditLog auditLog = auditLogMapper.toEntity(request);
        auditLog.setUserId(request.userId());
        AuditLog saved = auditLogRepository.save(auditLog);
        return auditLogMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuditLogResponse findById(UUID id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", id));
        return auditLogMapper.toResponse(auditLog);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(auditLogMapper::toResponse);
    }

    /** Server-side search over action, entityType, ipAddress (case-insensitive contains); blank q lists everything. */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(String q, Pageable pageable) {
        return auditLogRepository.findAll(TextSearch.<AuditLog>contains(q, "action", "entityType", "ipAddress"), pageable).map(auditLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findByUserId(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable).map(auditLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByEntity(String entityType, UUID entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }
}
