package com.fourati.repository;

import com.fourati.domain.LoanProductEligibilityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanProductEligibilityRuleRepository extends JpaRepository<LoanProductEligibilityRule, UUID> {

    List<LoanProductEligibilityRule> findByLoanProductId(UUID loanProductId);

    boolean existsByLoanProductId(UUID loanProductId);
}
