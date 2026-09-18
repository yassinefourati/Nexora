package com.fourati.repository;

import com.fourati.domain.ContractSignature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContractSignatureRepository extends JpaRepository<ContractSignature, UUID> {

    List<ContractSignature> findByLoanContractId(UUID loanContractId);

    boolean existsByLoanContractIdAndStatus(UUID loanContractId, String status);
}
