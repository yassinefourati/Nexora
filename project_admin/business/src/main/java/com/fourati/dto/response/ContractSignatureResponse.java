package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ContractSignatureResponse(
        UUID id,
        UUID loanContractId,
        String signerName,
        String signerRole,
        String status,
        String signatureMethod,
        String declineReason,
        Instant requestedAt,
        Instant signedAt,
        Instant declinedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
