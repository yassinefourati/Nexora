package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateCustomerEmploymentRequest(
        @NotNull
        UUID customerId,

        @Size(max = 200)
        @SafeInput
        String employerName,

        @Size(max = 150)
        @SafeInput
        String jobTitle,

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String employmentStatus,

        @DecimalMin(value = "0", inclusive = true)
        BigDecimal monthlyIncome,

        LocalDate startDate,

        LocalDate endDate
) {
}
