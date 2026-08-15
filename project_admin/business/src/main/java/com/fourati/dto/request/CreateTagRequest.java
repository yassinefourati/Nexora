package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @SafeInput @NotBlank @Size(max = 100) String name,
        @SafeInput @Size(max = 20) String color
) {
}
