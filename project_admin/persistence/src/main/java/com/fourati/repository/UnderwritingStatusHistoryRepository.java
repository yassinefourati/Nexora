package com.fourati.repository;

import com.fourati.domain.UnderwritingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UnderwritingStatusHistoryRepository extends JpaRepository<UnderwritingStatusHistory, UUID> {

    List<UnderwritingStatusHistory> findByUnderwritingCaseIdOrderByChangedAtAsc(UUID underwritingCaseId);
}
