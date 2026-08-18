package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.Size;

public record FinalizeLoanContractRequest(
        @Size(max = 500)
        @SafeInput
        String documentUrl
) {
}
