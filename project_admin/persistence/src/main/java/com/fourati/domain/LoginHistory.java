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

import java.time.Instant;

/**
 * Append-only record of a user's login attempt (login/logout timestamps,
 * origin IP, user agent, and whether the attempt succeeded).
 *
 * The real schema has no {@code location} column. Table has no
 * {@code deleted_at}/{@code created_by}/{@code updated_by} columns, so this
 * extends {@link AuditableEntity} rather than {@link BaseEntity}.
 */
@Entity
@Table(name = "login_history")
@Getter
@Setter
@NoArgsConstructor
public class LoginHistory extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Real column is varchar(45), not inet — see V14__ip_address_as_varchar.sql.
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Column(name = "login_at", nullable = false)
    private Instant loginAt;

    @Column(name = "logout_at")
    private Instant logoutAt;

    @Column(name = "success", nullable = false)
    private boolean success = true;
}
