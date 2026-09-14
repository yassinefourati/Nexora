package com.fourati.configuration.internal.persistence;

import com.fourati.configuration.internal.domain.MetadataKv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetadataKvRepository extends JpaRepository<MetadataKv, UUID>, JpaSpecificationExecutor<MetadataKv> {

    List<MetadataKv> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    Optional<MetadataKv> findByEntityTypeAndEntityIdAndKey(String entityType, UUID entityId, String key);

    boolean existsByEntityTypeAndEntityIdAndKey(String entityType, UUID entityId, String key);

    void deleteByEntityTypeAndEntityIdAndKey(String entityType, UUID entityId, String key);
}
