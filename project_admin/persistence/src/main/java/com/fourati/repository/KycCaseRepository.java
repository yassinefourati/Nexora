package com.fourati.repository;

import com.fourati.domain.KycCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface KycCaseRepository extends JpaRepository<KycCase, UUID>, JpaSpecificationExecutor<KycCase> {
}
