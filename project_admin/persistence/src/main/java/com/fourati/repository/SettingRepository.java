package com.fourati.repository;

import com.fourati.domain.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettingRepository extends JpaRepository<Setting, UUID>, JpaSpecificationExecutor<Setting> {

    boolean existsByKeyAndScopeAndOrganizationId(String key, String scope, UUID organizationId);

    boolean existsByKeyAndScopeAndOrganizationIdIsNull(String key, String scope);

    Optional<Setting> findByKeyAndScopeAndOrganizationId(String key, String scope, UUID organizationId);

    Optional<Setting> findByKeyAndScopeAndOrganizationIdIsNull(String key, String scope);

    List<Setting> findByScopeAndOrganizationId(String scope, UUID organizationId);

    List<Setting> findByScope(String scope);
}
