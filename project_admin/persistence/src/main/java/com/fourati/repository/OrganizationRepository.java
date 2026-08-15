package com.fourati.repository;

import com.fourati.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID>, JpaSpecificationExecutor<Organization> {

    boolean existsByCode(String code);

    Optional<Organization> findByCode(String code);

    List<Organization> findByParentOrganizationId(UUID parentOrganizationId);
}
