package com.fourati.repository;

import com.fourati.domain.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID>, JpaSpecificationExecutor<FeatureFlag> {

    boolean existsByKeyAndOrganizationId(String key, UUID organizationId);

    boolean existsByKeyAndOrganizationIdIsNull(String key);

    Optional<FeatureFlag> findByKeyAndOrganizationId(String key, UUID organizationId);

    Optional<FeatureFlag> findByKeyAndOrganizationIdIsNull(String key);

    List<FeatureFlag> findByOrganizationId(UUID organizationId);
}
