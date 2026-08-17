package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCustomerAddressRequest(
        @NotNull
        UUID customerId,

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String addressType,

        @NotBlank
        @Size(max = 200)
        @SafeInput
        String line1,

        @Size(max = 200)
        @SafeInput
        String line2,

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String city,

        @Size(max = 100)
        @SafeInput
        String state,

        @Size(max = 20)
        @SafeInput
        String postalCode,

        @NotBlank
        @Size(max = 2)
        @SafeInput
        String country,

        boolean primary
) {
}
