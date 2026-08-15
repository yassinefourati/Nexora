package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSettingRequest(

        String value,

        @Size(max = 500)
        @SafeInput
        String description,

        @NotNull
        Boolean editable
) {
}
