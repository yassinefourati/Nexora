package com.fourati.repository;

import com.fourati.domain.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {

    List<DocumentVersion> findByDocumentId(UUID documentId);

    boolean existsByDocumentIdAndVersionNumber(UUID documentId, int versionNumber);
}
