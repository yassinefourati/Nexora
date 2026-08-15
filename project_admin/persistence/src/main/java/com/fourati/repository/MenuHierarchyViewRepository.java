package com.fourati.repository;

import com.fourati.domain.MenuHierarchyView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.UUID;

/**
 * Read-only repository for {@code menu_hierarchy_view}. Extends the bare
 * {@link Repository} marker interface (not {@link org.springframework.data.jpa.repository.JpaRepository})
 * so no save/delete methods are ever generated against this database view.
 */
public interface MenuHierarchyViewRepository extends Repository<MenuHierarchyView, UUID> {

    Page<MenuHierarchyView> findAll(Pageable pageable);

    Page<MenuHierarchyView> findByMenuId(UUID menuId, Pageable pageable);
}
