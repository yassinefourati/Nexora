package com.fourati.access.internal.persistence;

import com.fourati.access.internal.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID>, JpaSpecificationExecutor<Menu> {

    boolean existsByCode(String code);

    Optional<Menu> findByCode(String code);
}
