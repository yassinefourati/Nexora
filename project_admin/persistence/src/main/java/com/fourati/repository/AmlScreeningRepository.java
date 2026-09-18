package com.fourati.repository;

import com.fourati.domain.AmlScreening;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AmlScreeningRepository extends JpaRepository<AmlScreening, UUID> {

    List<AmlScreening> findByKycCaseId(UUID kycCaseId);
}
