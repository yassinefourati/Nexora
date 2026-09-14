package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateContractSignatureRequest(
        @NotNull
        UUID loanContractId,

        @NotBlank
        @Size(max = 200)
        @SafeInput
        String signerName,

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String signerRole,

        @Size(max = 20)
        @SafeInput
        String signatureMethod
) {
}
