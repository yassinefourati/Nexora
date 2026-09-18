package com.fourati.repository;

import com.fourati.domain.LoanDisbursementStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanDisbursementStatusHistoryRepository extends JpaRepository<LoanDisbursementStatusHistory, UUID> {

    List<LoanDisbursementStatusHistory> findByLoanDisbursementIdOrderByChangedAtAsc(UUID loanDisbursementId);
}
