package com.fourati.audit.internal.persistence;

import com.fourati.audit.internal.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);
}
