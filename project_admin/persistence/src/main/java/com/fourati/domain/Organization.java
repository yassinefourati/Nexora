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
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

/**
 * An organization (tenant-like grouping). May be nested via
 * {@code parentOrganization}.
 *
 * Table has no {@code created_by}/{@code updated_by} columns in the real
 * schema, so this extends {@link SoftDeletableEntity} rather than
 * {@link BaseEntity}.
 *
 * Uniqueness of {@code code} is enforced in the database via a partial
 * unique index ({@code uq_organizations_code ... WHERE deleted_at IS NULL})
 * — see V2__org_structure.sql. There is no table-level unique constraint to
 * mirror here via {@code @Column(unique=true)}.
 */
@Entity
@Table(name = "organizations")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Organization extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_organization_id")
    private Organization parentOrganization;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "active";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    private String metadata = "{}";
}
