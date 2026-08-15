package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.Size;

public record UpdatePermissionRequest(

        @Size(max = 255)
        @SafeInput
        String description
) {
}
