package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CollectionNoteResponse(
        UUID id,
        UUID collectionCaseId,
        String author,
        String note,
        Instant createdAt,
        Instant updatedAt
) {
}
