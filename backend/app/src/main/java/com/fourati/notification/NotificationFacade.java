package com.fourati.notification;

import java.util.UUID;

/**
 * What other modules may ask the notification domain. Ownership questions
 * are answered here so the identity module's authorization beans never touch
 * notification's repositories (ADR 0004).
 */
public interface NotificationFacade {

    /** True when the user-notification row exists and belongs to {@code userId}. */
    boolean isNotificationOwner(UUID userNotificationId, UUID userId);
}
