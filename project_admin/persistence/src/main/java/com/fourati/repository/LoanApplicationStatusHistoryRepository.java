package com.fourati.repository;

import com.fourati.domain.LoanApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanApplicationStatusHistoryRepository extends JpaRepository<LoanApplicationStatusHistory, UUID> {

    List<LoanApplicationStatusHistory> findByLoanApplicationIdOrderByChangedAtAsc(UUID loanApplicationId);
}
