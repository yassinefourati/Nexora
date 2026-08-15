package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Join entity granting a permission to a role, with optional conditions.
 * Note: role_permissions has no deleted_at/created_by/updated_by column in the
 * real schema, so this extends the leaner {@link AuditableEntity} rather than
 * {@link BaseEntity}.
 */
@Entity
@Table(name = "role_permissions")
public class RolePermission extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditions", nullable = false, columnDefinition = "jsonb")
    private String conditions = "{}";

    public RolePermission() {
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }
}
