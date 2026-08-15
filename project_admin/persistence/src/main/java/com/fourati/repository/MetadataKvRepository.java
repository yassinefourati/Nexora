package com.fourati.repository;

import com.fourati.domain.MetadataKv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetadataKvRepository extends JpaRepository<MetadataKv, UUID> {

    List<MetadataKv> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    Optional<MetadataKv> findByEntityTypeAndEntityIdAndKey(String entityType, UUID entityId, String key);

    boolean existsByEntityTypeAndEntityIdAndKey(String entityType, UUID entityId, String key);

    void deleteByEntityTypeAndEntityIdAndKey(String entityType, UUID entityId, String key);
}
