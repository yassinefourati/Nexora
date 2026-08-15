package com.fourati.repository;

import com.fourati.domain.ApiKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    Optional<ApiKey> findByKeyHash(String keyHash);

    boolean existsByKeyHash(String keyHash);

    Page<ApiKey> findByUserId(UUID userId, Pageable pageable);
}
