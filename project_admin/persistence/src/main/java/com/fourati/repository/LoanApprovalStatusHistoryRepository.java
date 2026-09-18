package com.fourati.repository;

import com.fourati.domain.LoanApprovalStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanApprovalStatusHistoryRepository extends JpaRepository<LoanApprovalStatusHistory, UUID> {

    List<LoanApprovalStatusHistory> findByLoanApprovalIdOrderByChangedAtAsc(UUID loanApprovalId);
}
