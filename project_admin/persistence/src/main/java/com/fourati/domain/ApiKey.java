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
import java.util.ArrayList;
import java.util.List;

/**
 * A programmatic API key issued to a user.
 *
 * Note: the real schema has no {@code prefix} column and no
 * {@code organization_id} column -- only {@code key_hash} is persisted (the
 * plaintext secret is generated server-side, returned once to the caller, and
 * never stored). Table has no {@code deleted_at}/{@code created_by}/
 * {@code updated_by} columns, so this extends {@link AuditableEntity} rather
 * than {@link BaseEntity}.
 */
@Entity
@Table(name = "api_keys")
@Getter
@Setter
@NoArgsConstructor
public class ApiKey extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "key_hash", nullable = false, unique = true, columnDefinition = "text")
    private String keyHash;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "scopes", nullable = false, columnDefinition = "text[]")
    private List<String> scopes = new ArrayList<>();

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;
}
