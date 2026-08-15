package com.fourati.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite identifier for {@link UserRolesView}, matching the natural key of
 * {@code user_roles_view} (see V12__real_schema_views.sql): a user can hold
 * the same role in at most one organization scope at a time (enforced by the
 * {@code uq_user_roles_global}/{@code uq_user_roles_org} partial unique
 * indexes on the underlying {@code user_roles} table -- see V1__rbac_core.sql),
 * so (userId, roleId, organizationId) is unique per row. organizationId may be
 * {@code null} for a global role assignment.
 */
public class UserRolesViewId implements Serializable {

    private UUID userId;
    private UUID roleId;
    private UUID organizationId;

    public UserRolesViewId() {
    }

    public UserRolesViewId(UUID userId, UUID roleId, UUID organizationId) {
        this.userId = userId;
        this.roleId = roleId;
        this.organizationId = organizationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRolesViewId that)) return false;
        return Objects.equals(userId, that.userId)
                && Objects.equals(roleId, that.roleId)
                && Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId, organizationId);
    }
}
