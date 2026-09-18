package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanNotification;
import com.fourati.domain.Notification;
import com.fourati.dto.request.CreateLoanNotificationRequest;
import com.fourati.dto.response.LoanNotificationResponse;
import com.fourati.mapper.LoanNotificationMapper;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanNotificationRepository;
import com.fourati.repository.NotificationRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Sends a notification about a loan application's lifecycle event, reusing
 * the platform's existing {@link Notification} content/delivery
 * infrastructure (see {@link NotificationService}) rather than duplicating
 * it — this service only renders the {@link Notification} row and links it
 * to the triggering {@link LoanApplication} with an event type.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoanNotificationService {

    private final LoanNotificationRepository loanNotificationRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final NotificationRepository notificationRepository;
    private final LoanNotificationMapper loanNotificationMapper;

    @Audited(action = "CREATE", description = "Send a notification about a loan application event")
    public LoanNotificationResponse create(CreateLoanNotificationRequest request) {
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));

        Notification notification = new Notification();
        notification.setTitle(request.title());
        notification.setBody(request.body());
        notification.setChannel(request.channel());
        Notification savedNotification = notificationRepository.save(notification);

        LoanNotification entity = new LoanNotification();
        entity.setLoanApplication(loanApplication);
        entity.setNotification(savedNotification);
        entity.setEventType(request.eventType());
        LoanNotification saved = loanNotificationRepository.save(entity);
        return loanNotificationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LoanNotificationResponse> findByLoanApplicationId(UUID loanApplicationId) {
        return loanNotificationRepository.findByLoanApplicationIdOrderByCreatedAtDesc(loanApplicationId).stream()
                .map(loanNotificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<LoanNotificationResponse> findAll(Pageable pageable) {
        return loanNotificationRepository.findAll(pageable).map(loanNotificationMapper::toResponse);
    }
}
