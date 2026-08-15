package com.fourati.repository;

import com.fourati.domain.AppModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppModuleRepository extends JpaRepository<AppModule, UUID> {

    boolean existsByKey(String key);

    Optional<AppModule> findByKey(String key);

    List<AppModule> findByActiveTrue();
}
