package com.fourati.identity.internal.persistence;

import com.fourati.identity.internal.domain.ApiKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID>, JpaSpecificationExecutor<ApiKey> {

    Optional<ApiKey> findByKeyHash(String keyHash);

    boolean existsByKeyHash(String keyHash);

    Page<ApiKey> findByUserId(UUID userId, Pageable pageable);

    java.util.List<ApiKey> findByUserIdAndRevokedAtIsNull(UUID userId);
}
