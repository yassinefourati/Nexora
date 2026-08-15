package com.fourati.repository;

import com.fourati.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID>, JpaSpecificationExecutor<Team> {

    boolean existsByOrganizationIdAndName(UUID organizationId, String name);

    List<Team> findByOrganizationId(UUID organizationId);

    List<Team> findByDepartmentId(UUID departmentId);
}
