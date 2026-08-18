package com.fourati.repository;

import com.fourati.domain.LoanAccountStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanAccountStatusHistoryRepository extends JpaRepository<LoanAccountStatusHistory, UUID> {

    List<LoanAccountStatusHistory> findByLoanAccountIdOrderByChangedAtAsc(UUID loanAccountId);
}
