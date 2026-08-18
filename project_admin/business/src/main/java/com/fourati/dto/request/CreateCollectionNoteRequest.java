package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCollectionNoteRequest(
        @NotNull
        UUID collectionCaseId,

        @NotBlank
        @Size(max = 150)
        @SafeInput
        String author,

        @NotBlank
        @Size(max = 2000)
        @SafeInput
        String note
) {
}
