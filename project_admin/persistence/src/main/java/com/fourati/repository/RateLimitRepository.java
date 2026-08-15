package com.fourati.repository;

import com.fourati.domain.RateLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RateLimitRepository extends JpaRepository<RateLimit, UUID> {

    Optional<RateLimit> findByScopeKeyAndWindowStartedAt(String scopeKey, Instant windowStartedAt);

    boolean existsByScopeKeyAndWindowStartedAt(String scopeKey, Instant windowStartedAt);
}
