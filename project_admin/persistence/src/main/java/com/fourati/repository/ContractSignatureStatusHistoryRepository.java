package com.fourati.repository;

import com.fourati.domain.ContractSignatureStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContractSignatureStatusHistoryRepository extends JpaRepository<ContractSignatureStatusHistory, UUID> {

    List<ContractSignatureStatusHistory> findByContractSignatureIdOrderByChangedAtAsc(UUID contractSignatureId);
}
