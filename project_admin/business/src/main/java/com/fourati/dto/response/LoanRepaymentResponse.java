package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanRepaymentResponse(
        UUID id,
        UUID loanAccountId,
        UUID loanInstallmentId,
        String status,
        BigDecimal amount,
        String paymentMethod,
        String referenceNumber,
        String failureReason,
        Instant paidAt,
        Instant failedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
