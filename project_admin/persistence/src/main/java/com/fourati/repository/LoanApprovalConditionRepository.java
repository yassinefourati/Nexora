package com.fourati.repository;

import com.fourati.domain.LoanApprovalCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanApprovalConditionRepository extends JpaRepository<LoanApprovalCondition, UUID> {

    List<LoanApprovalCondition> findByLoanApprovalId(UUID loanApprovalId);
}
