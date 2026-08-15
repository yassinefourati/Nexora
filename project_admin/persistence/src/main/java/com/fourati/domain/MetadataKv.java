package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * A single key/value pair attached to an arbitrary polymorphic entity,
 * identified by (entityType, entityId). {@code value} is a required JSONB
 * column in the real schema.
 */
@Entity
@Table(name = "metadata_kv")
@Getter
@Setter
@NoArgsConstructor
public class MetadataKv extends ContentExtensionBaseEntity {

    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "key", length = 150, nullable = false)
    private String key;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", nullable = false, columnDefinition = "jsonb")
    private String value;
}
