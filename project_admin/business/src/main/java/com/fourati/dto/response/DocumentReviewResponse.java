package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DocumentReviewResponse(
        UUID id,
        UUID documentId,
        String decision,
        String comments,
        Instant reviewedAt
) {
}
