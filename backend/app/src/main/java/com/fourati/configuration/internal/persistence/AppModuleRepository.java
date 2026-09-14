package com.fourati.configuration.internal.persistence;

import com.fourati.configuration.internal.domain.AppModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppModuleRepository extends JpaRepository<AppModule, UUID>, JpaSpecificationExecutor<AppModule> {

    boolean existsByKey(String key);

    Optional<AppModule> findByKey(String key);

    List<AppModule> findByActiveTrue();
}
