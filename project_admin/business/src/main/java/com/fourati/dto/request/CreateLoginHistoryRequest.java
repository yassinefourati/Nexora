package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateLoginHistoryRequest(

        @NotNull
        UUID userId,

        @Size(max = 45)
        @SafeInput
        String ipAddress,

        @Size(max = 500)
        @SafeInput
        String userAgent,

        Instant loginAt,

        Boolean success
) {
}
