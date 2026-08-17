package com.fourati.repository;

import com.fourati.domain.KycStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KycStatusHistoryRepository extends JpaRepository<KycStatusHistory, UUID> {

    List<KycStatusHistory> findByKycCaseIdOrderByChangedAtAsc(UUID kycCaseId);
}
