package com.fourati.repository;

import com.fourati.domain.RiskAssessmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RiskAssessmentStatusHistoryRepository extends JpaRepository<RiskAssessmentStatusHistory, UUID> {

    List<RiskAssessmentStatusHistory> findByRiskAssessmentIdOrderByChangedAtAsc(UUID riskAssessmentId);
}
