package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only mapping of {@code active_users_view} (see V12__real_schema_views.sql):
 * active, non-deleted users. Reproduces the view's exact column list --
 * {@code id, username, email, first_name, last_name, last_login_at, created_at}.
 *
 * {@code id} is the users table's own primary key, so it is genuinely unique
 * within this view (the view only filters rows, it never joins/fans out), and
 * can safely be used as the JPA {@code @Id} with no generation strategy --
 * Hibernate must never attempt to INSERT/UPDATE against a database view.
 */
@Entity
@Table(name = "active_users_view")
@Immutable
@Getter
@NoArgsConstructor
public class ActiveUsersView {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "username")
    private String username;

    @Column(name = "email", columnDefinition = "citext")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at")
    private Instant createdAt;
}
