package com.fourati.repository;

import com.fourati.domain.Permission;
import com.fourati.domain.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    List<RolePermission> findByRoleId(UUID roleId);

    List<RolePermission> findByPermissionId(UUID permissionId);

    Optional<RolePermission> findByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    boolean existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    void deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    @Query("""
            select distinct rp.permission
            from RolePermission rp
            where rp.role.name in :roleNames
            """)
    List<Permission> findDistinctPermissionsByRoleNameIn(@Param("roleNames") Collection<String> roleNames);
}
