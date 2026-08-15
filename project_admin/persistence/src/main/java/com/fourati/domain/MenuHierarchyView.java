package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

/**
 * Read-only mapping of {@code menu_hierarchy_view} (see
 * V12__real_schema_views.sql): the recursive-CTE-built full menu item tree,
 * with each row's depth and " > "-joined ancestor path. Reproduces the view's
 * exact column list -- {@code id, menu_id, parent_menu_item_id, label,
 * route_path, sort_order, is_active, depth, path}.
 *
 * {@code id} is the menu_items table's own primary key -- the recursive CTE
 * walks parent/child links but each menu item still appears exactly once, so
 * it remains a valid, unique JPA {@code @Id} for this view.
 */
@Entity
@Table(name = "menu_hierarchy_view")
@Immutable
@Getter
@NoArgsConstructor
public class MenuHierarchyView {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "menu_id")
    private UUID menuId;

    @Column(name = "parent_menu_item_id")
    private UUID parentMenuItemId;

    @Column(name = "label")
    private String label;

    @Column(name = "route_path")
    private String routePath;

    @Column(name = "sort_order")
    private int sortOrder;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "depth")
    private int depth;

    @Column(name = "path")
    private String path;
}
