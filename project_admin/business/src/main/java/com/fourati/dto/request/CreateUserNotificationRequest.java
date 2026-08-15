package com.fourati.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateUserNotificationRequest(

        @NotNull
        UUID userId,

        @NotNull
        UUID notificationId
) {
}
