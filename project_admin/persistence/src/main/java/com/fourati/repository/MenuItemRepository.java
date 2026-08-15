package com.fourati.repository;

import com.fourati.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID>, JpaSpecificationExecutor<MenuItem> {

    List<MenuItem> findByMenuId(UUID menuId);

    List<MenuItem> findByParentMenuItemId(UUID parentMenuItemId);

    List<MenuItem> findByMenuIdAndParentMenuItemIsNull(UUID menuId);
}
