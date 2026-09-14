package com.fourati.content.internal.persistence;

import com.fourati.content.internal.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID>, JpaSpecificationExecutor<Comment> {

    Page<Comment> findByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable);

    List<Comment> findByParentCommentId(UUID parentCommentId);

    Page<Comment> findByUserId(UUID userId, Pageable pageable);

    boolean existsByIdAndUserId(UUID id, UUID userId);
}
