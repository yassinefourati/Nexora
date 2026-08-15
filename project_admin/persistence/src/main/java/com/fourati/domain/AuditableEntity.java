package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Leaner alternative to {@link BaseEntity} for tables in the real schema that
 * have {@code id}, {@code created_at}, {@code updated_at} but genuinely no
 * {@code deleted_at} / {@code created_by} / {@code updated_by} columns (per
 * db_admin-202608131126.sql). Mapping the full {@link BaseEntity} onto such a
 * table would make Hibernate's {@code ddl-auto=validate} check fail at boot
 * since those columns don't exist physically.
 *
 * Fields provided:
 *   id         — UUID primary key
 *   createdAt  — creation timestamp (set once)
 *   updatedAt  — last modification timestamp (also kept current in the DB by
 *                the {@code set_updated_at()} trigger on tables that have one)
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
