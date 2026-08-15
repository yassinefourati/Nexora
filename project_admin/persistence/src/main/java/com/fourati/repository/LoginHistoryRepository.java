package com.fourati.repository;

import com.fourati.domain.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID> {

    Page<LoginHistory> findByUserId(UUID userId, Pageable pageable);

    Optional<LoginHistory> findByIdAndUserId(UUID id, UUID userId);
}
