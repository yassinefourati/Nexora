package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Application user account.
 *
 * Note: email is backed by a Postgres {@code citext} column (case-insensitive
 * text) in the real schema. It is mapped here as a plain String -- Postgres
 * handles the case-insensitive comparison/uniqueness semantics itself.
 */
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
public class User extends SoftDeletableEntity {

    @Column(name = "username", nullable = false, length = 100, unique = true)
    private String username;

    @Column(name = "email", nullable = false, columnDefinition = "citext", unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    private String passwordHash;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "active";

    @Column(name = "is_superuser", nullable = false)
    private boolean superuser = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    private String metadata = "{}";

    public User() {
    }

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(Instant passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSuperuser() {
        return superuser;
    }

    public void setSuperuser(boolean superuser) {
        this.superuser = superuser;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
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
