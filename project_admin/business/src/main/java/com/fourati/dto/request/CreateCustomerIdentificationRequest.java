package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateCustomerIdentificationRequest(
        @NotNull
        UUID customerId,

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String idType,

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String idNumber,

        @NotBlank
        @Size(max = 2)
        @SafeInput
        String issuingCountry,

        LocalDate issueDate,

        LocalDate expiryDate
) {
}
