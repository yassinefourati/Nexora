package com.fourati.repository;

import com.fourati.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID>, JpaSpecificationExecutor<Department> {

    boolean existsByOrganizationIdAndCode(UUID organizationId, String code);

    List<Department> findByOrganizationId(UUID organizationId);

    List<Department> findByParentDepartmentId(UUID parentDepartmentId);
}
