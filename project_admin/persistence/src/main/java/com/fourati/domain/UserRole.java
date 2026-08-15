package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Join entity assigning a role to a user, optionally scoped to an organization.
 * organization_id has no FK constraint yet at this migration group (the
 * organizations table does not exist here) -- it is a plain UUID column here;
 * the ManyToOne to Organization is wired once that entity/migration exists.
 * Note: user_roles has no deleted_at/created_by/updated_by column in the real
 * schema, so this extends the leaner {@link AuditableEntity} rather than
 * {@link BaseEntity}.
 */
@Entity
@Table(name = "user_roles")
public class UserRole extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    public UserRole() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }
}
