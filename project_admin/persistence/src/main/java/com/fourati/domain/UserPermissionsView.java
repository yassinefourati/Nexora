package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

/**
 * Read-only mapping of {@code user_permissions_view} (see
 * V12__real_schema_views.sql): effective permissions granted to each user
 * through their (non-expired) role assignments. Reproduces the view's exact
 * column list -- {@code user_id, username, resource, action, permission_code,
 * organization_id}.
 *
 * Deliberately mapped to the live (non-materialized) view rather than
 * {@code user_permissions_mv} -- the materialized view is created
 * {@code WITH NO DATA} (see V12) and has no scheduled refresh wired up in
 * this stage, so reading it would return an empty/stale result set. If a
 * refresh job is introduced later, the mv is a drop-in replacement for
 * higher-traffic read paths (same column shape, same natural key -- see
 * {@link UserPermissionsViewId}).
 */
@Entity
@Table(name = "user_permissions_view")
@IdClass(UserPermissionsViewId.class)
@Immutable
@Getter
@NoArgsConstructor
public class UserPermissionsView {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username")
    private String username;

    @Column(name = "resource")
    private String resource;

    @Column(name = "action")
    private String action;

    @Id
    @Column(name = "permission_code")
    private String permissionCode;

    @Id
    @Column(name = "organization_id")
    private UUID organizationId;
}
