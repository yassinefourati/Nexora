package com.fourati.repository;

import com.fourati.domain.LoanOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoanOfferRepository extends JpaRepository<LoanOffer, UUID> {

    Optional<LoanOffer> findByLoanApplicationId(UUID loanApplicationId);

    boolean existsByLoanApplicationId(UUID loanApplicationId);
}
