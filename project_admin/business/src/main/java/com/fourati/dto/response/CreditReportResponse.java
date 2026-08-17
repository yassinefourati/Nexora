package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CreditReportResponse(
        UUID id,
        UUID creditCheckId,
        String bureauName,
        String reportReference,
        Integer rawScore,
        Instant retrievedAt
) {
}
