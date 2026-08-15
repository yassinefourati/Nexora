package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateDepartmentRequest(
        UUID parentDepartmentId,

        @NotBlank
        @Size(max = 200)
        @SafeInput
        String name,

        @Size(max = 50)
        @SafeInput
        String code
) {
}
