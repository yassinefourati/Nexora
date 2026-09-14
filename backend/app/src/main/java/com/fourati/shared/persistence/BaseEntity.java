package com.fourati.shared.persistence;


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
 * Base class for all domain entities.
 *
 * Fields provided:
 *   id         — UUID primary key
 *   createdAt  — creation timestamp (set once)
 *   updatedAt  — last modification timestamp
 *
 * The real schema has no created_by/updated_by column on any table, so those
 * are intentionally not modeled here (there is no audit-actor column to map
 * them to — application-level auditing, if needed, goes through audit_logs
 * instead). Soft-delete is opt-in via {@link SoftDeletableEntity} for the
 * handful of tables that actually have a deleted_at column; entities for
 * tables without one must extend this class directly, not that subclass.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

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
