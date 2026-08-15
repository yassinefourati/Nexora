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

/**
 * Join entity granting a role visibility into a menu item.
 * Note: role_menus has no deleted_at column, so no soft-delete restriction is applied.
 */
@Entity
@Table(name = "role_menus")
@Getter
@Setter
@NoArgsConstructor
public class RoleMenu extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "can_view", nullable = false)
    private boolean canView = true;
}
