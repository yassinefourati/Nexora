package com.fourati.repository;

import com.fourati.domain.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleMenuRepository extends JpaRepository<RoleMenu, UUID> {

    List<RoleMenu> findByRoleId(UUID roleId);

    List<RoleMenu> findByMenuItemId(UUID menuItemId);

    Optional<RoleMenu> findByRoleIdAndMenuItemId(UUID roleId, UUID menuItemId);

    boolean existsByRoleIdAndMenuItemId(UUID roleId, UUID menuItemId);
}
