package com.fourati.repository;

import com.fourati.domain.LoanRepaymentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanRepaymentStatusHistoryRepository extends JpaRepository<LoanRepaymentStatusHistory, UUID> {

    List<LoanRepaymentStatusHistory> findByLoanRepaymentIdOrderByChangedAtAsc(UUID loanRepaymentId);
}
