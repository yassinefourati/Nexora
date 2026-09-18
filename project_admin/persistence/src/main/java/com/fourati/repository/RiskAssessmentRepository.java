package com.fourati.repository;

import com.fourati.domain.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID>, JpaSpecificationExecutor<RiskAssessment> {

    boolean existsByLoanApplicationId(UUID loanApplicationId);
}
