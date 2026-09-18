package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCustomerRequest(
        @NotBlank
        @Size(max = 20)
        @SafeInput
        String customerType,

        @Size(max = 20)
        @SafeInput
        String status,

        @Size(max = 100)
        @SafeInput
        String firstName,

        @Size(max = 100)
        @SafeInput
        String lastName,

        @Size(max = 200)
        @SafeInput
        String businessName,

        LocalDate dateOfBirth,

        @Size(max = 50)
        @SafeInput
        String nationalId,

        @NotBlank
        @Email
        @Size(max = 320)
        String email,

        @Size(max = 30)
        @SafeInput
        String phone
) {
}
