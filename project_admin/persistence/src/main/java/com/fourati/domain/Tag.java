package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * A label that can be attached to arbitrary entities via {@link EntityTag}.
 * tags has deleted_at (soft-delete) in the real schema, so it extends
 * {@link SoftDeletableEntity}.
 */
@Entity
@Table(name = "tags")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Tag extends SoftDeletableEntity {

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "color", length = 20)
    private String color;
}
