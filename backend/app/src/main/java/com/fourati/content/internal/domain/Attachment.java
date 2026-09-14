package com.fourati.content.internal.domain;


import com.fourati.shared.persistence.ContentExtensionBaseEntity;

import com.fourati.shared.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * A file attached to an arbitrary polymorphic entity, identified by
 * (entityType, entityId).
 *
 * Note: attachments has no deleted_at column in the real schema (hard-deleted, not
 * soft-deleted), so no {@code @SQLRestriction} is applied here.
 */
@Entity
@Table(name = "attachments")
@Getter
@Setter
@NoArgsConstructor
public class Attachment extends ContentExtensionBaseEntity {

    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    /** Uploader as a plain user id: users live in the identity module (ADR 0004). */
    @Column(name = "uploaded_by")
    private UUID uploadedBy;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "file_url", nullable = false, columnDefinition = "text")
    private String fileUrl;

    @Column(name = "mime_type", length = 150)
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;
}
