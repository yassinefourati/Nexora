package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateLoanApprovalConditionRequest(
        @NotNull
        UUID loanApprovalId,

        @NotBlank
        @Size(max = 500)
        @SafeInput
        String description
) {
}
