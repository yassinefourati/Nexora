package com.fourati.audit.internal.persistence;

import com.fourati.audit.internal.domain.SystemEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface SystemEventRepository extends JpaRepository<SystemEvent, UUID>, JpaSpecificationExecutor<SystemEvent> {

    Page<SystemEvent> findByEventType(String eventType, Pageable pageable);

    Page<SystemEvent> findBySeverity(String severity, Pageable pageable);
}
