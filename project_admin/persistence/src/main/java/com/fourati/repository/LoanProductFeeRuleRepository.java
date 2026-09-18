package com.fourati.repository;

import com.fourati.domain.LoanProductFeeRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanProductFeeRuleRepository extends JpaRepository<LoanProductFeeRule, UUID> {

    List<LoanProductFeeRule> findByLoanProductId(UUID loanProductId);
}
