package com.fourati.repository;

import com.fourati.domain.CreditCheckStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CreditCheckStatusHistoryRepository extends JpaRepository<CreditCheckStatusHistory, UUID> {

    List<CreditCheckStatusHistory> findByCreditCheckIdOrderByChangedAtAsc(UUID creditCheckId);
}
