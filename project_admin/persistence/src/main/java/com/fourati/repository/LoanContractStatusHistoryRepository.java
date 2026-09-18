package com.fourati.repository;

import com.fourati.domain.LoanContractStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanContractStatusHistoryRepository extends JpaRepository<LoanContractStatusHistory, UUID> {

    List<LoanContractStatusHistory> findByLoanContractIdOrderByChangedAtAsc(UUID loanContractId);
}
