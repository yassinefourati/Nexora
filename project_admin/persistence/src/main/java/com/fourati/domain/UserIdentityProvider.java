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

import java.time.Instant;

/**
 * Links a user to an external identity provider account (e.g. SSO / OAuth2 provider).
 *
 * The real schema has no separate {@code email} column -- provider-supplied
 * profile data is captured via {@code raw_profile} (jsonb) instead. Uniqueness
 * is enforced in the database only on {@code (provider, provider_user_id)} --
 * there is no additional {@code (user_id, provider)} unique constraint.
 *
 * Table has no {@code deleted_at}/{@code created_by}/{@code updated_by} columns,
 * so this extends {@link AuditableEntity} rather than {@link BaseEntity}.
 */
@Entity
@Table(name = "user_identity_providers")
@Getter
@Setter
@NoArgsConstructor
public class UserIdentityProvider extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_profile", nullable = false, columnDefinition = "jsonb")
    private String rawProfile = "{}";

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;
}
