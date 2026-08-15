package com.fourati.repository;

import com.fourati.domain.EntityTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntityTagRepository extends JpaRepository<EntityTag, UUID> {

    List<EntityTag> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    List<EntityTag> findByTagId(UUID tagId);

    Optional<EntityTag> findByTagIdAndEntityTypeAndEntityId(UUID tagId, String entityType, UUID entityId);

    boolean existsByTagIdAndEntityTypeAndEntityId(UUID tagId, String entityType, UUID entityId);

    void deleteByEntityTypeAndEntityId(String entityType, UUID entityId);
}
