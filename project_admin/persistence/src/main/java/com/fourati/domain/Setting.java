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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A configuration setting, either global or scoped to an organization.
 *
 * Uniqueness is enforced in the database via two partial unique indexes
 * (global settings unique by {@code (scope, key)}, org-scoped settings unique
 * by {@code (scope, organization_id, key)}) — see V3__app_config.sql. The
 * database also enforces {@code scope IN ('global','organization')} and a
 * cross-column check tying {@code scope} to {@code organization_id} nullness;
 * those are DB-level invariants only, not duplicated here.
 *
 * Table has no {@code deleted_at}/{@code created_by}/{@code updated_by} columns
 * in the real schema, so this extends {@link AuditableEntity} rather than
 * {@link BaseEntity}.
 */
@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
public class Setting extends AuditableEntity {

    @Column(name = "scope", nullable = false, length = 20)
    private String scope = "global";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "key", nullable = false, length = 150)
    private String key;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", nullable = false, columnDefinition = "jsonb")
    private String value;

    @Column(name = "description")
    private String description;

    @Column(name = "is_editable", nullable = false)
    private boolean editable = true;
}
