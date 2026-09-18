package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteLoanRepaymentRequest(
        @NotBlank
        @Size(max = 100)
        @SafeInput
        String referenceNumber
) {
}
