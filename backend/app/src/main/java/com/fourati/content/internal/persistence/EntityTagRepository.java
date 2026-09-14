package com.fourati.content.internal.persistence;


import com.fourati.content.internal.domain.EntityTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntityTagRepository extends JpaRepository<EntityTag, UUID>, JpaSpecificationExecutor<EntityTag> {

    List<EntityTag> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    List<EntityTag> findByTagId(UUID tagId);

    Optional<EntityTag> findByTagIdAndEntityTypeAndEntityId(UUID tagId, String entityType, UUID entityId);

    boolean existsByTagIdAndEntityTypeAndEntityId(UUID tagId, String entityType, UUID entityId);

    void deleteByEntityTypeAndEntityId(String entityType, UUID entityId);
}
