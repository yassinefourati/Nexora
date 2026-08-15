package com.fourati.repository;

import com.fourati.domain.ErrorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, UUID>, JpaSpecificationExecutor<ErrorLog> {

    Page<ErrorLog> findBySeverity(String severity, Pageable pageable);

    Page<ErrorLog> findBySource(String source, Pageable pageable);
}
