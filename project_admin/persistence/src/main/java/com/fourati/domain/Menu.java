package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * A navigation menu. Menus are global (not organization-scoped) in the real schema.
 */
@Entity
@Table(name = "menus")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Menu extends SoftDeletableEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
