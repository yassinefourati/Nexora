package com.fourati.organization.internal.persistence;

import com.fourati.organization.internal.domain.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID>, JpaSpecificationExecutor<OrganizationMember> {

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    List<OrganizationMember> findByOrganizationId(UUID organizationId);

    List<OrganizationMember> findByUserId(UUID userId);

    List<OrganizationMember> findByDepartmentId(UUID departmentId);

    List<OrganizationMember> findByTeamId(UUID teamId);
}
