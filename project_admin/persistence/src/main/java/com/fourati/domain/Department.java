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
import org.hibernate.annotations.SQLRestriction;

/**
 * A department within an organization. May be nested via
 * {@code parentDepartment}.
 *
 * Table has no {@code created_by}/{@code updated_by} columns in the real
 * schema, so this extends {@link SoftDeletableEntity} rather than
 * {@link BaseEntity}.
 *
 * Uniqueness of {@code code} within an organization is enforced in the
 * database via a partial unique index ({@code uq_departments_org_code ...
 * WHERE deleted_at IS NULL AND code IS NOT NULL}) — see
 * V2__org_structure.sql.
 */
@Entity
@Table(name = "departments")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Department extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "code", length = 50)
    private String code;
}
