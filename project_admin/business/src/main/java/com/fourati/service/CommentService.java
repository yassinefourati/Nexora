package com.fourati.service;

import com.fourati.domain.Comment;
import com.fourati.domain.User;
import com.fourati.dto.request.CreateCommentRequest;
import com.fourati.dto.request.UpdateCommentRequest;
import com.fourati.dto.response.CommentResponse;
import com.fourati.mapper.CommentMapper;
import com.fourati.repository.CommentRepository;
import com.fourati.repository.UserRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Audited(action = "CREATE", description = "Create a new comment")
    public CommentResponse create(UUID userId, CreateCommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Comment entity = commentMapper.toEntity(request);
        entity.setUser(user);
        if (request.parentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", request.parentCommentId()));
            entity.setParentComment(parentComment);
        }
        Comment saved = commentRepository.save(entity);
        return commentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CommentResponse findById(UUID id) {
        return commentMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> findByEntity(String entityType, UUID entityId, Pageable pageable) {
        return commentRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
                .map(commentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> findByUser(UUID userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable).map(commentMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Update a comment")
    public CommentResponse update(UUID id, UpdateCommentRequest request) {
        Comment entity = getEntityOrThrow(id);
        commentMapper.updateEntityFromRequest(request, entity);
        Comment saved = commentRepository.save(entity);
        return commentMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Delete a comment")
    public void delete(UUID id) {
        Comment entity = getEntityOrThrow(id);
        commentRepository.delete(entity);
    }

    private Comment getEntityOrThrow(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
    }
}
