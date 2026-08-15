package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateOrganizationMemberRequest(
        @NotNull
        UUID organizationId,

        @NotNull
        UUID userId,

        UUID departmentId,

        UUID teamId,

        @Size(max = 150)
        @SafeInput
        String title,

        boolean primary
) {
}
