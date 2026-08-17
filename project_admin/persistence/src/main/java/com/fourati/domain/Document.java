package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

/**
 * Metadata and object-storage reference for an uploaded document. File
 * bytes live in external object storage (MinIO) — this row never holds
 * file content, only {@code storageKey}.
 */
@Entity
@Table(name = "documents")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Document extends SoftDeletableEntity {

    @Column(name = "document_type", length = 30, nullable = false)
    private String documentType;

    @Column(name = "category", length = 30, nullable = false)
    private String category;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "storage_key", length = 500, nullable = false)
    private String storageKey;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "uploaded";

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt = Instant.now();
}
