package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

/**
 * Extends {@link BaseEntity} with a {@code deleted_at} column, for the subset
 * of tables in the real schema that actually support soft delete: users,
 * roles, organizations, departments, teams, menus, menu_items, tags. Every
 * other entity must extend {@link BaseEntity} directly.
 *
 * Subclasses should add {@code @SQLRestriction("deleted_at IS NULL")} for
 * transparent soft-delete filtering at the JPA layer.
 */
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseEntity {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
