package com.fourati.repository;

import com.fourati.domain.DocumentReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentReviewRepository extends JpaRepository<DocumentReview, UUID> {

    List<DocumentReview> findByDocumentIdOrderByReviewedAtAsc(UUID documentId);
}
