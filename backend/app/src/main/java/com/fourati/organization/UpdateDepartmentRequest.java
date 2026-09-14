package com.fourati.organization;

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
        String code,
        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
