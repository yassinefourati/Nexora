package com.fourati.repository;

import com.fourati.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable);

    List<Comment> findByParentCommentId(UUID parentCommentId);

    Page<Comment> findByUserId(UUID userId, Pageable pageable);
}
