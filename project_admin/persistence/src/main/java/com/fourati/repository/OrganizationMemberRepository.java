package com.fourati.repository;

import com.fourati.domain.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    List<OrganizationMember> findByOrganizationId(UUID organizationId);

    List<OrganizationMember> findByUserId(UUID userId);

    List<OrganizationMember> findByDepartmentId(UUID departmentId);

    List<OrganizationMember> findByTeamId(UUID teamId);
}
