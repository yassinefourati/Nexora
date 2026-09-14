package com.fourati.access.internal.persistence;

import com.fourati.access.internal.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID>, JpaSpecificationExecutor<Permission> {

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByResourceAndAction(String resource, String action);
}
