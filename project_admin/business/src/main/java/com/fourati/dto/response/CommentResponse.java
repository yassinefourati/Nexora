package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String entityType,
        UUID entityId,
        UUID userId,
        String body,
        UUID parentCommentId,
        Instant createdAt,
        Instant updatedAt
) {
}
