package com.fourati.repository;

import com.fourati.domain.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUserId(UUID userId);

    Page<UserRole> findByRoleId(UUID roleId, Pageable pageable);

    Optional<UserRole> findByUserIdAndRoleIdAndOrganizationId(UUID userId, UUID roleId, UUID organizationId);

    boolean existsByUserIdAndRoleIdAndOrganizationId(UUID userId, UUID roleId, UUID organizationId);
}
