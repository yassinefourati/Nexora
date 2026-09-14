package com.fourati.repository;

import com.fourati.domain.UnderwritingNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UnderwritingNoteRepository extends JpaRepository<UnderwritingNote, UUID> {

    List<UnderwritingNote> findByUnderwritingCaseIdOrderByCreatedAtAsc(UUID underwritingCaseId);
}
