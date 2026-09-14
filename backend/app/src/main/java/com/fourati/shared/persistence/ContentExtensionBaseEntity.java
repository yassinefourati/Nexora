package com.fourati.shared.persistence;

import com.fourati.content.internal.domain.Tag;

import com.fourati.configuration.internal.domain.MetadataKv;

import com.fourati.content.internal.domain.EntityTag;

import com.fourati.content.internal.domain.Comment;

import com.fourati.content.internal.domain.Attachment;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Leaner base class for content-extension tables that, per the real schema, have
 * only {@code id}, {@code created_at}, {@code updated_at} -- no {@code created_by}/
 * {@code updated_by} (not attributable to an end-user actor in this schema) and no
 * {@code deleted_at} (they are hard-deleted, not soft-deleted).
 *
 * Unlike {@link BaseEntity}, this does not declare {@code deleted_at}/{@code created_by}/
 * {@code updated_by} columns, since those columns genuinely do not exist on
 * entity_tags, comments, attachments, or metadata_kv in the real schema -- declaring
 * them here would break {@code spring.jpa.hibernate.ddl-auto=validate} at boot.
 *
 * {@code created_at}/{@code updated_at} are still managed via Spring Data JPA
 * Auditing, same as {@link BaseEntity}, since both columns are populated by the
 * application (DEFAULT now() covers the initial insert; updated_at is refreshed by
 * JPA auditing on update).
 *
 * Used by: {@link EntityTag}, {@link Comment}, {@link Attachment}, {@link MetadataKv}.
 * Note: {@link Tag} does NOT use this base -- tags has a deleted_at column (but still
 * no created_by/updated_by), so it declares its own fields directly.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class ContentExtensionBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Optimistic-lock counter (see V16). Hibernate bumps it on every update and rejects stale writes. */
    @jakarta.persistence.Version
    @Column(name = "version", nullable = false)
    private long version;

    public long getVersion() {
        return version;
    }
}
