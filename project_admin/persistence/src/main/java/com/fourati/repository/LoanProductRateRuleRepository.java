package com.fourati.repository;

import com.fourati.domain.LoanProductRateRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanProductRateRuleRepository extends JpaRepository<LoanProductRateRule, UUID> {

    List<LoanProductRateRule> findByLoanProductId(UUID loanProductId);
}
