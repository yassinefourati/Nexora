package com.fourati.identity.internal.persistence;

import com.fourati.identity.internal.domain.AuthLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AuthLogRepository extends JpaRepository<AuthLog, UUID>, JpaSpecificationExecutor<AuthLog> {

    Page<AuthLog> findByUserId(UUID userId, Pageable pageable);

    Page<AuthLog> findByEventType(String eventType, Pageable pageable);
}
