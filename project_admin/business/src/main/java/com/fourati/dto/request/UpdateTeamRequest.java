package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateTeamRequest(
        UUID departmentId,

        @NotBlank
        @Size(max = 200)
        @SafeInput
        String name
) {
}
