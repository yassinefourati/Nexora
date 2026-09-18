package com.fourati.repository;

import com.fourati.domain.CollectionCaseStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CollectionCaseStatusHistoryRepository extends JpaRepository<CollectionCaseStatusHistory, UUID> {

    List<CollectionCaseStatusHistory> findByCollectionCaseIdOrderByChangedAtAsc(UUID collectionCaseId);
}
