package com.fourati.identity.internal.persistence;

import com.fourati.identity.internal.domain.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID>, JpaSpecificationExecutor<Session> {

    Optional<Session> findByTokenHash(String tokenHash);

    boolean existsByTokenHash(String tokenHash);

    Page<Session> findByUserId(UUID userId, Pageable pageable);

    boolean existsByIdAndUserUsername(UUID id, String username);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    List<Session> findByUserIdAndRevokedAtIsNull(UUID userId);
}
