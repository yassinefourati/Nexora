package com.fourati.repository;

import com.fourati.domain.CollectionNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CollectionNoteRepository extends JpaRepository<CollectionNote, UUID> {

    List<CollectionNote> findByCollectionCaseIdOrderByCreatedAtAsc(UUID collectionCaseId);
}
