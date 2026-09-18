package com.fourati.repository;

import com.fourati.domain.LoanProductVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanProductVersionRepository extends JpaRepository<LoanProductVersion, UUID> {

    List<LoanProductVersion> findByLoanProductId(UUID loanProductId);

    boolean existsByLoanProductIdAndVersionNumber(UUID loanProductId, int versionNumber);
}
