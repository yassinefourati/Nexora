package com.fourati.repository;

import com.fourati.domain.MenuPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuPermissionRepository extends JpaRepository<MenuPermission, UUID> {

    List<MenuPermission> findByMenuItemId(UUID menuItemId);

    List<MenuPermission> findByPermissionId(UUID permissionId);

    Optional<MenuPermission> findByMenuItemIdAndPermissionId(UUID menuItemId, UUID permissionId);

    boolean existsByMenuItemIdAndPermissionId(UUID menuItemId, UUID permissionId);
}
