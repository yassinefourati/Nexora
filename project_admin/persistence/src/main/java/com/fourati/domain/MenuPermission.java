package com.fourati.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Join entity associating a permission required to view/use a menu item.
 * Note: menu_permissions has no deleted_at column, so no soft-delete restriction is applied.
 */
@Entity
@Table(name = "menu_permissions")
@Getter
@Setter
@NoArgsConstructor
public class MenuPermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
}
