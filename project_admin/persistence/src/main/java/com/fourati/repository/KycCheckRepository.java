package com.fourati.repository;

import com.fourati.domain.KycCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KycCheckRepository extends JpaRepository<KycCheck, UUID> {

    List<KycCheck> findByKycCaseId(UUID kycCaseId);
}
