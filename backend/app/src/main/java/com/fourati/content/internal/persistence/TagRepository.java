package com.fourati.content.internal.persistence;

import com.fourati.content.internal.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID>, JpaSpecificationExecutor<Tag> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);
}
