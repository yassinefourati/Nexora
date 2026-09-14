package com.fourati.configuration.internal.domain;

import com.fourati.organization.internal.domain.Organization;

import java.util.UUID;

import com.fourati.shared.persistence.AuditableEntity;

import com.fourati.shared.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A feature flag, optionally scoped to an organization, controlling whether
 * a feature is enabled.
 *
 * Uniqueness is enforced in the database via two partial unique indexes
 * (global flags unique by {@code key}, org-scoped flags unique by
 * {@code (key, organization_id)}) — see V3__app_config.sql. There is no
 * table-level unique constraint to mirror here via {@code @Column(unique=true)}.
 *
 * Table has no {@code deleted_at}/{@code created_by}/{@code updated_by} columns
 * in the real schema, so this extends {@link AuditableEntity} rather than
 * {@link BaseEntity}.
 */
@Entity
@Table(name = "feature_flags")
@Getter
@Setter
@NoArgsConstructor
public class FeatureFlag extends AuditableEntity {

    @Column(name = "key", nullable = false, length = 150)
    private String key;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description")
    private String description;

    /** Organization scope as a plain id: organizations live in another module (ADR 0004). */
    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = false;
}
