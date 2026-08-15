package com.fourati.repository;

import com.fourati.domain.AuthLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthLogRepository extends JpaRepository<AuthLog, UUID> {

    Page<AuthLog> findByUserId(UUID userId, Pageable pageable);

    Page<AuthLog> findByEventType(String eventType, Pageable pageable);
}
