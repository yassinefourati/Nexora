package com.fourati.repository;

import com.fourati.domain.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByTokenHash(String tokenHash);

    boolean existsByTokenHash(String tokenHash);

    Page<Session> findByUserId(UUID userId, Pageable pageable);

    boolean existsByIdAndUserUsername(UUID id, String username);
}
