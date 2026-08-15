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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Append-only record of authentication-related events (login attempts, logouts,
 * password resets, MFA challenges, etc.) for security auditing purposes.
 *
 * {@code event_type} is restricted in the database to a fixed set of values via
 * a CHECK constraint (login_success, login_failed, logout, password_reset,
 * password_change, mfa_challenge, account_locked, account_unlocked) -- not
 * duplicated here. The real schema has no {@code email}/{@code failure_reason}
 * columns; free-form context is captured via {@code metadata} (jsonb) instead.
 *
 * Table has no {@code deleted_at}/{@code created_by}/{@code updated_by} columns,
 * so this extends {@link AuditableEntity} rather than {@link BaseEntity}.
 */
@Entity
@Table(name = "auth_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuthLog extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    // Real column is varchar(45), not inet — see V14__ip_address_as_varchar.sql.
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    private String metadata = "{}";
}
