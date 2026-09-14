package com.fourati.notification.internal.application;

import com.fourati.notification.NotificationFacade;
import com.fourati.notification.internal.persistence.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class NotificationFacadeImpl implements NotificationFacade {

    private final UserNotificationRepository userNotificationRepository;

    @Override
    public boolean isNotificationOwner(UUID userNotificationId, UUID userId) {
        return userNotificationId != null && userId != null
                && userNotificationRepository.existsByIdAndUserId(userNotificationId, userId);
    }
}
