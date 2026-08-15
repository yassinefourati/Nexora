package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

/**
 * A fine-grained permission identified by resource + action.
 *
 * Note: permissions has no deleted_at/created_by/updated_by column in the real
 * schema, so this extends the leaner {@link AuditableEntity} rather than
 * {@link BaseEntity}. "code" is a real Postgres GENERATED ALWAYS AS
 * (resource || '.' || action) STORED column -- it is never written by
 * Hibernate (insertable = false, updatable = false) and must never be
 * computed/set from Java before insert; Postgres always derives it.
 * {@code @Generated(INSERT)} tells Hibernate to re-select the column right
 * after INSERT so the in-memory entity (and thus the create-response DTO)
 * reflects the database-computed value instead of staying null.
 */
@Entity
@Table(name = "permissions")
public class Permission extends AuditableEntity {

    @Column(name = "resource", nullable = false, length = 100)
    private String resource;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Generated(event = EventType.INSERT)
    @Column(name = "code", insertable = false, updatable = false, length = 150)
    private String code;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    public Permission() {
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
