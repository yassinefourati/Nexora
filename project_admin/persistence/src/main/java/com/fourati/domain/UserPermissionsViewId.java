package com.fourati.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite identifier for {@link UserPermissionsView}, matching
 * {@code user_permissions_view}'s natural key (userId, permissionCode,
 * organizationId) -- the same triple the real schema's
 * {@code uq_user_permissions_mv} unique index (see V12__real_schema_views.sql)
 * uses for {@code user_permissions_mv}. organizationId may be {@code null}
 * for a globally-scoped role assignment.
 */
public class UserPermissionsViewId implements Serializable {

    private UUID userId;
    private String permissionCode;
    private UUID organizationId;

    public UserPermissionsViewId() {
    }

    public UserPermissionsViewId(UUID userId, String permissionCode, UUID organizationId) {
        this.userId = userId;
        this.permissionCode = permissionCode;
        this.organizationId = organizationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPermissionsViewId that)) return false;
        return Objects.equals(userId, that.userId)
                && Objects.equals(permissionCode, that.permissionCode)
                && Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, permissionCode, organizationId);
    }
}
