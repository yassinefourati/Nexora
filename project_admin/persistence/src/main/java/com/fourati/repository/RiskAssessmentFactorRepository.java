package com.fourati.repository;

import com.fourati.domain.RiskAssessmentFactor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RiskAssessmentFactorRepository extends JpaRepository<RiskAssessmentFactor, UUID> {

    List<RiskAssessmentFactor> findByRiskAssessmentId(UUID riskAssessmentId);
}
