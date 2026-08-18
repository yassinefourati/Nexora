package com.fourati.repository;

import com.fourati.domain.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, UUID> {

    List<LoanRepayment> findByLoanAccountId(UUID loanAccountId);

    boolean existsByLoanInstallmentIdAndStatus(UUID loanInstallmentId, String status);
}
