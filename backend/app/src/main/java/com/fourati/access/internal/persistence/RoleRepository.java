package com.fourati.access.internal.persistence;

import com.fourati.access.internal.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID>, JpaSpecificationExecutor<Role> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}
