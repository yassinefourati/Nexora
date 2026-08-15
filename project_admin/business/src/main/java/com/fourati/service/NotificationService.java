package com.fourati.service;

import com.fourati.domain.Notification;
import com.fourati.domain.NotificationTemplate;
import com.fourati.dto.request.CreateNotificationRequest;
import com.fourati.dto.request.UpdateNotificationRequest;
import com.fourati.dto.response.NotificationResponse;
import com.fourati.mapper.NotificationMapper;
import com.fourati.repository.NotificationRepository;
import com.fourati.repository.NotificationTemplateRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Notifications carry only the rendered content (title/body/channel/data);
 * per-recipient delivery/read tracking lives on {@link com.fourati.domain.UserNotification}
 * (see {@link UserNotificationService}), not on this entity.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationMapper notificationMapper;

    @Audited(action = "CREATE", description = "Create a notification")
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification entity = notificationMapper.toEntity(request);

        if (request.templateId() != null) {
            NotificationTemplate template = notificationTemplateRepository.findById(request.templateId())
                    .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate", request.templateId()));
            entity.setTemplate(template);
        }

        Notification saved = notificationRepository.save(entity);
        return notificationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public NotificationResponse findById(UUID id) {
        Notification entity = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        return notificationMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> findAll(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> findByChannel(String channel, Pageable pageable) {
        return notificationRepository.findByChannel(channel, pageable).map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> findByTemplateId(UUID templateId, Pageable pageable) {
        return notificationRepository.findByTemplateId(templateId, pageable).map(notificationMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Update a notification")
    public NotificationResponse update(UUID id, UpdateNotificationRequest request) {
        Notification entity = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));

        notificationMapper.updateEntityFromRequest(request, entity);
        Notification saved = notificationRepository.save(entity);
        return notificationMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Delete a notification")
    public void delete(UUID id) {
        Notification entity = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        notificationRepository.delete(entity);
    }
}
