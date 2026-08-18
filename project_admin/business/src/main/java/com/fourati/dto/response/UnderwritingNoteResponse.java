package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UnderwritingNoteResponse(
        UUID id,
        UUID underwritingCaseId,
        String author,
        String note,
        Instant createdAt,
        Instant updatedAt
) {
}
