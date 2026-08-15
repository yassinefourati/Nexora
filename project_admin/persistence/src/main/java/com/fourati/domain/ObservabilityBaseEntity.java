package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Leaner base class for append-only observability tables that, per the real schema,
 * have only {@code id}, {@code created_at}, {@code updated_at} — no {@code deleted_at}
 * (they are never soft-deleted) and no {@code created_by}/{@code updated_by} (they are
 * written internally by the application, not attributable to an end-user actor).
 *
 * Unlike {@link BaseEntity}, {@code created_at}/{@code updated_at} here are NOT managed
 * by Spring Data JPA Auditing — the real schema sets {@code created_at} via a column
 * DEFAULT and {@code updated_at} via a {@code set_updated_at()} DB trigger on UPDATE, so
 * the entity only needs to read them back, not compute them.
 *
 * Used by: {@link ErrorLog}, {@link SystemEvent}.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class ObservabilityBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
