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
 * A comment attached to an arbitrary polymorphic entity, identified by
 * (entityType, entityId). Supports threaded replies via {@code parentComment}.
 *
 * Note: comments has no deleted_at column in the real schema (hard-deleted, not
 * soft-deleted), so no {@code @SQLRestriction} is applied here.
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
public class Comment extends ContentExtensionBaseEntity {

    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Column(name = "body", nullable = false, columnDefinition = "text")
    private String body;
}
