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

/**
 * A free-text note left by a collector on a {@link CollectionCase}. Table
 * has no {@code deleted_at} column, so this extends {@link BaseEntity}
 * directly.
 */
@Entity
@Table(name = "collection_notes")
@Getter
@Setter
@NoArgsConstructor
public class CollectionNote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_case_id", nullable = false)
    private CollectionCase collectionCase;

    @Column(name = "author", length = 150, nullable = false)
    private String author;

    @Column(name = "note", length = 2000, nullable = false)
    private String note;
}
