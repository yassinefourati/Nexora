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
import org.hibernate.annotations.SQLRestriction;

/**
 * A single navigable item within a menu, optionally nested under a parent
 * menu item via parent_menu_item_id. Not organization-scoped in the real schema.
 */
@Entity
@Table(name = "menu_items")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class MenuItem extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_menu_item_id")
    private MenuItem parentMenuItem;

    @Column(name = "label", nullable = false, length = 150)
    private String label;

    @Column(name = "route_path", length = 255)
    private String routePath;

    @Column(name = "module_key", length = 100)
    private String moduleKey;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
