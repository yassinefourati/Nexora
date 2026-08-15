package com.fourati.repository;

import com.fourati.domain.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    List<Attachment> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    Page<Attachment> findByUploadedById(UUID uploadedById, Pageable pageable);
}
