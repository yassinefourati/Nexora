package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an application module (feature area) that can be enabled/disabled
 * or referenced by other configuration entities.
 *
 * Table has no {@code deleted_at}/{@code created_by}/{@code updated_by} columns
 * in the real schema, so this extends {@link AuditableEntity} rather than
 * {@link BaseEntity}.
 */
@Entity
@Table(name = "app_modules")
@Getter
@Setter
@NoArgsConstructor
public class AppModule extends AuditableEntity {

    @Column(name = "key", nullable = false, unique = true, length = 100)
    private String key;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
