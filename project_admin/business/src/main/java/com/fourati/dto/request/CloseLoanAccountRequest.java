package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.Size;

public record CloseLoanAccountRequest(
        @Size(max = 500)
        @SafeInput
        String reason
) {
}
