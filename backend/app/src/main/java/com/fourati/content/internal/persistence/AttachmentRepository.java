package com.fourati.content.internal.persistence;

import com.fourati.content.internal.domain.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID>, JpaSpecificationExecutor<Attachment> {

    List<Attachment> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    Page<Attachment> findByUploadedBy(UUID uploadedById, Pageable pageable);

    boolean existsByIdAndUploadedBy(UUID id, UUID userId);
}
