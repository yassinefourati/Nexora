package com.fourati.service;

import com.fourati.domain.Notification;
import com.fourati.domain.User;
import com.fourati.domain.UserNotification;
import com.fourati.dto.request.CreateUserNotificationRequest;
import com.fourati.dto.response.UserNotificationResponse;
import com.fourati.error.BusinessRuleException;
import com.fourati.error.ErrorCode;
import com.fourati.mapper.UserNotificationMapper;
import com.fourati.repository.NotificationRepository;
import com.fourati.repository.UserNotificationRepository;
import com.fourati.repository.UserRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserNotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final UserNotificationMapper userNotificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Audited(action = "CREATE", description = "Assign a notification to a user")
    public UserNotificationResponse create(CreateUserNotificationRequest request) {
        if (userNotificationRepository.existsByUserIdAndNotificationId(request.userId(), request.notificationId())) {
            throw new BusinessRuleException(ErrorCode.USER_NOTIFICATION_ALREADY_EXISTS,
                    "Notification " + request.notificationId() + " is already assigned to user " + request.userId());
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));
        Notification notification = notificationRepository.findById(request.notificationId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", request.notificationId()));

        UserNotification entity = userNotificationMapper.toEntity(request);
        entity.setUser(user);
        entity.setNotification(notification);
        entity.setRead(false);
        entity.setDeliveredAt(Instant.now());

        UserNotification saved = userNotificationRepository.save(entity);
        UserNotificationResponse response = userNotificationMapper.toResponse(saved);

        // Live push to the recipient, if they have an active WebSocket session —
        // this is additive, not a replacement for the REST-polling model the
        // frontend already has; a client that missed the push (offline, or
        // connecting later) still sees it on its next GET /user-notifications.
        messagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/notifications", response);

        return response;
    }

    @Transactional(readOnly = true)
    public UserNotificationResponse findById(UUID id) {
        UserNotification entity = userNotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserNotification", id));
        return userNotificationMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<UserNotificationResponse> findAll(Pageable pageable) {
        return userNotificationRepository.findAll(pageable).map(userNotificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserNotificationResponse> findByUserId(UUID userId, Pageable pageable) {
        return userNotificationRepository.findByUserId(userId, pageable).map(userNotificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserNotificationResponse> findByUserIdAndRead(UUID userId, boolean read, Pageable pageable) {
        return userNotificationRepository.findByUserIdAndRead(userId, read, pageable)
                .map(userNotificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return userNotificationRepository.countByUserIdAndRead(userId, false);
    }

    @Audited(action = "UPDATE", description = "Mark a user notification as read")
    public UserNotificationResponse markRead(UUID id) {
        UserNotification entity = userNotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserNotification", id));
        if (!entity.isRead()) {
            entity.setRead(true);
            entity.setReadAt(Instant.now());
        }
        UserNotification saved = userNotificationRepository.save(entity);
        return userNotificationMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Remove a user notification assignment")
    public void delete(UUID id) {
        UserNotification entity = userNotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserNotification", id));
        userNotificationRepository.delete(entity);
    }
}
