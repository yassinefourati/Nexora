package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Associates a {@link Tag} with an arbitrary polymorphic entity, identified
 * by (entityType, entityId). No JPA relation is mapped for the polymorphic
 * side by design.
 */
@Entity
@Table(name = "entity_tags")
@Getter
@Setter
@NoArgsConstructor
public class EntityTag extends ContentExtensionBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;
}
