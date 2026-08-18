package com.fourati.repository;

import com.fourati.domain.LoanOfferStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanOfferStatusHistoryRepository extends JpaRepository<LoanOfferStatusHistory, UUID> {

    List<LoanOfferStatusHistory> findByLoanOfferIdOrderByChangedAtAsc(UUID loanOfferId);
}
