package com.fourati.service;

import com.fourati.domain.NotificationTemplate;
import com.fourati.dto.request.CreateNotificationTemplateRequest;
import com.fourati.dto.request.UpdateNotificationTemplateRequest;
import com.fourati.dto.response.NotificationTemplateResponse;
import com.fourati.mapper.NotificationTemplateMapper;
import com.fourati.repository.NotificationTemplateRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationTemplateService {

    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationTemplateMapper notificationTemplateMapper;

    @Audited(action = "CREATE", description = "Create a notification template")
    public NotificationTemplateResponse create(CreateNotificationTemplateRequest request) {
        if (notificationTemplateRepository.existsByCode(request.code())) {
            throw new ConflictException("Notification template with code '" + request.code() + "' already exists");
        }
        NotificationTemplate entity = notificationTemplateMapper.toEntity(request);
        NotificationTemplate saved = notificationTemplateRepository.save(entity);
        return notificationTemplateMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public NotificationTemplateResponse findById(UUID id) {
        NotificationTemplate entity = notificationTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate", id));
        return notificationTemplateMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<NotificationTemplateResponse> findAll(Pageable pageable) {
        return notificationTemplateRepository.findAll(pageable)
                .map(notificationTemplateMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Update a notification template")
    public NotificationTemplateResponse update(UUID id, UpdateNotificationTemplateRequest request) {
        NotificationTemplate entity = notificationTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate", id));
        notificationTemplateMapper.updateEntityFromRequest(request, entity);
        NotificationTemplate saved = notificationTemplateRepository.save(entity);
        return notificationTemplateMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Delete a notification template")
    public void delete(UUID id) {
        NotificationTemplate entity = notificationTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate", id));
        notificationTemplateRepository.delete(entity);
    }
}
