package com.fourati.repository;

import com.fourati.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);
}
