package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String resource,

        @NotBlank
        @Size(max = 20)
        @Pattern(regexp = "read|write|edit|delete|execute|approve",
                message = "action must be one of: read, write, edit, delete, execute, approve")
        String action,

        @Size(max = 255)
        @SafeInput
        String description
) {
}
