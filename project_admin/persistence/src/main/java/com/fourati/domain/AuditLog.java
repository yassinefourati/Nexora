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

import java.util.UUID;

/**
 * Immutable audit trail record capturing a create/update/delete action performed
 * on any entity in the system. Append-only from the API's perspective.
 *
 * The real table is genuinely partitioned ({@code PARTITION BY RANGE (created_at)},
 * 3 monthly partitions plus a DEFAULT partition -- see V5__audit_security_logs.sql)
 * with a composite physical primary key {@code (id, created_at)}. JPA does not need
 * to be aware of the partitioning -- Postgres routes INSERTs to the correct child
 * partition transparently based on {@code created_at}. The entity keeps {@code @Id}
 * on {@code id} alone (generated via {@code gen_random_uuid()} and therefore unique
 * in practice), which is the pragmatic, compiling approach against the real table.
 *
 * Table has no {@code deleted_at}/{@code created_by}/{@code updated_by} columns, so
 * this extends {@link AuditableEntity} rather than {@link BaseEntity}.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_data", columnDefinition = "jsonb")
    private String beforeData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_data", columnDefinition = "jsonb")
    private String afterData;

    // Real column is varchar(45), not inet — see V14__ip_address_as_varchar.sql.
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}
