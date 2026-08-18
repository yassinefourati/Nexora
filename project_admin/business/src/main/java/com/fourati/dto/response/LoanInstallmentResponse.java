package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LoanInstallmentResponse(
        UUID id,
        UUID loanAccountId,
        int installmentNumber,
        LocalDate dueDate,
        BigDecimal principalAmount,
        BigDecimal interestAmount,
        BigDecimal totalAmount,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
