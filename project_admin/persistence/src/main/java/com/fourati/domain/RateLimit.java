package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Internal rate-limit counter tracking request counts for a scope key within a
 * time window. Not exposed via a public REST API.
 *
 * The real schema is much simpler than previously modeled -- no
 * {@code key}/{@code user_id}/{@code ip_address}/{@code endpoint}/
 * {@code window_end} columns. Just {@code scope_key} + {@code window_started_at}
 * (unique together) + {@code request_count}. Table has no {@code deleted_at}/
 * {@code created_by}/{@code updated_by} columns, so this extends
 * {@link AuditableEntity} rather than {@link BaseEntity}.
 */
@Entity
@Table(name = "rate_limits")
@Getter
@Setter
@NoArgsConstructor
public class RateLimit extends AuditableEntity {

    @Column(name = "scope_key", nullable = false, length = 255)
    private String scopeKey;

    @Column(name = "window_started_at", nullable = false)
    private Instant windowStartedAt;

    @Column(name = "request_count", nullable = false)
    private Integer requestCount = 1;
}
