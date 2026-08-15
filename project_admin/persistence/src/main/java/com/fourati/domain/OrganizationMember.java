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
 * Join entity linking a user to an organization as a member, optionally
 * scoped to a department and/or team within that organization.
 *
 * Table has no {@code deleted_at}/{@code created_by}/{@code updated_by}
 * columns in the real schema, so this extends {@link AuditableEntity}
 * rather than {@link BaseEntity} — membership rows are hard-deleted when
 * removed.
 */
@Entity
@Table(name = "organization_members")
@Getter
@Setter
@NoArgsConstructor
public class OrganizationMember extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "title", length = 150)
    private String title;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt = Instant.now();
}
