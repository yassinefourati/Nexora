package com.fourati.content.internal.web;

import com.fourati.shared.api.ApiConstants;
import com.fourati.content.CreateCommentRequest;
import com.fourati.content.UpdateCommentRequest;
import com.fourati.content.CommentResponse;
import com.fourati.content.internal.application.CommentService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/comments")
@Tag(name = "Comments", description = "Threaded comments attached to arbitrary polymorphic entities.")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#userId, authentication)")
    @Operation(summary = "Create a new comment")
    public ResponseEntity<ApiResponse<CommentResponse>> create(@RequestParam UUID userId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse created = commentService.create(userId, request);
        return ApiResponse.created(created, "Comment created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a comment by id")
    public ResponseEntity<ApiResponse<CommentResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(commentService.findById(id), "Comment retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List comments for an entity (paginated)")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> listByEntity(
            @RequestParam String entityType, @RequestParam UUID entityId, Pageable pageable) {
        return ApiResponse.paged(commentService.findByEntity(entityType, entityId, pageable), "Comments retrieved");
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List comments authored by a user (paginated)")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> listByUser(@PathVariable UUID userId,
            Pageable pageable) {
        return ApiResponse.paged(commentService.findByUser(userId, pageable), "Comments retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isCommentAuthor(#id, authentication)")
    @Operation(summary = "Update a comment")
    public ResponseEntity<ApiResponse<CommentResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateCommentRequest request) {
        return ApiResponse.ok(commentService.update(id, request), "Comment updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isCommentAuthor(#id, authentication)")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        commentService.delete(id);
        return ApiResponse.noContent();
    }
}
