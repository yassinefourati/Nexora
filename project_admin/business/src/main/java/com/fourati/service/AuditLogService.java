package com.fourati.service;

import com.fourati.domain.AuditLog;
import com.fourati.domain.User;
import com.fourati.dto.request.CreateAuditLogRequest;
import com.fourati.dto.response.AuditLogResponse;
import com.fourati.mapper.AuditLogMapper;
import com.fourati.repository.AuditLogRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
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

    @Audited(action = "CREATE", description = "Recorded a new audit log entry")
    public AuditLogResponse record(CreateAuditLogRequest request) {
        AuditLog auditLog = auditLogMapper.toEntity(request);
        if (request.userId() != null) {
            User user = new User();
            user.setId(request.userId());
            auditLog.setUser(user);
        }
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
