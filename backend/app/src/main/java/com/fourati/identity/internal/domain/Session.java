package com.fourati.identity.internal.domain;

import com.fourati.platform.error.ErrorCode;

import com.fourati.platform.error.BusinessRuleException;

import com.fourati.shared.persistence.AuditableEntity;

import com.fourati.shared.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A refresh/auth session issued to a user, identified by a hashed token.
 *
 * The real schema has no {@code issued_at} column (use {@code createdAt} from
 * {@link AuditableEntity} instead). Table has no {@code deleted_at}/
 * {@code created_by}/{@code updated_by} columns, so this extends
 * {@link AuditableEntity} rather than {@link BaseEntity}.
 */
@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
public class Session extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, columnDefinition = "text")
    private String tokenHash;

    // Real column is varchar(45), not inet — see V14__ip_address_as_varchar.sql.
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    /** True while neither revoked nor expired at {@code now}. */
    public boolean isLive(Instant now) {
        return revokedAt == null && (expiresAt == null || expiresAt.isAfter(now));
    }

    /** Revokes at {@code now}; a second revocation is a business-rule error, not a silent no-op. */
    public void revoke(Instant now) {
        if (revokedAt != null) {
            throw new BusinessRuleException(ErrorCode.SESSION_ALREADY_REVOKED, "Session " + getId() + " was already revoked at " + revokedAt);
        }
        this.revokedAt = now;
    }
}
