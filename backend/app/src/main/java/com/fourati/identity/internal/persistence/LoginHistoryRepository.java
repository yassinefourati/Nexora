package com.fourati.identity.internal.persistence;

import com.fourati.identity.internal.domain.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID>, JpaSpecificationExecutor<LoginHistory> {

    Page<LoginHistory> findByUserId(UUID userId, Pageable pageable);

    Optional<LoginHistory> findByIdAndUserId(UUID id, UUID userId);
}
