package com.fourati.notification;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateUserNotificationRequest(

        @NotNull
        UUID userId,

        @NotNull
        UUID notificationId
) {
}
