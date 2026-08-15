package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only mapping of {@code user_roles_view} (see V12__real_schema_views.sql):
 * users joined with their role assignments and (optionally) the scoping
 * organization. Reproduces the view's exact column list --
 * {@code user_id, username, role_id, role_name, organization_id,
 * organization_name, expires_at}.
 *
 * The view has no single surrogate key column of its own; (userId, roleId,
 * organizationId) is the natural key -- see {@link UserRolesViewId}.
 */
@Entity
@Table(name = "user_roles_view")
@IdClass(UserRolesViewId.class)
@Immutable
@Getter
@NoArgsConstructor
public class UserRolesView {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username")
    private String username;

    @Id
    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "role_name")
    private String roleName;

    @Id
    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
