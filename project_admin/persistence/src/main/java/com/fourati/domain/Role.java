package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLRestriction;

/**
 * A role that groups permissions and can be assigned to users.
 */
@Entity
@Table(name = "roles")
@SQLRestriction("deleted_at IS NULL")
public class Role extends SoftDeletableEntity {

    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "is_system", nullable = false)
    private boolean system = false;

    public Role() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }
}
