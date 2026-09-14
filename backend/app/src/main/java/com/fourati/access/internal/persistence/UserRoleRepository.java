package com.fourati.access.internal.persistence;

import com.fourati.access.internal.domain.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID>, JpaSpecificationExecutor<UserRole> {

    List<UserRole> findByUserId(UUID userId);

    Page<UserRole> findByRoleId(UUID roleId, Pageable pageable);

    Optional<UserRole> findByUserIdAndRoleIdAndOrganizationId(UUID userId, UUID roleId, UUID organizationId);

    boolean existsByUserIdAndRoleIdAndOrganizationId(UUID userId, UUID roleId, UUID organizationId);
}
