package com.fourati.identity.internal.domain;

import com.fourati.identity.UserStatus;
import com.fourati.shared.persistence.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

/**
 * Application user account. Authentication is Keycloak's job (ADR 0001):
 * this row exists to hang roles, notifications, sessions and audit actors on,
 * and to carry the account's lifecycle ({@link UserStatus}) which the
 * aggregate itself protects.
 *
 * Note: email is backed by a Postgres {@code citext} column (case-insensitive
 * text); Postgres handles the case-insensitive comparison and uniqueness.
 */
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
public class User extends SoftDeletableEntity {

    @Column(name = "username", nullable = false, length = 100, unique = true)
    private String username;

    @Column(name = "email", nullable = false, columnDefinition = "citext", unique = true)
    private String email;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "is_superuser", nullable = false)
    private boolean superuser = false;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    private String metadata = "{}";

    public User() {
    }

    // ── behaviour ──────────────────────────────────────────────────────────

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    /** Cut off by an administrator: keeps rows and grants, cannot be granted new roles. Idempotent. */
    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    /** Back in service from any non-active state. Idempotent. */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    /** Switched off without the "cut off" connotation of a suspension. Idempotent. */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    /** Administrative status change from the edit form; null means "leave as is". */
    public void changeStatus(UserStatus target) {
        if (target != null) {
            this.status = target;
        }
    }

    /** Hides the row from every read path while keeping it for audit history. */
    public void softDelete(Instant now) {
        setDeletedAt(Objects.requireNonNull(now));
    }

    // ── accessors ──────────────────────────────────────────────────────────

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status == null ? UserStatus.ACTIVE : status;
    }

    public boolean isSuperuser() {
        return superuser;
    }

    public void setSuperuser(boolean superuser) {
        this.superuser = superuser;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
