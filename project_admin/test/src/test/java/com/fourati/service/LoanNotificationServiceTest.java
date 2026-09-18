package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanNotification;
import com.fourati.domain.Notification;
import com.fourati.dto.request.CreateLoanNotificationRequest;
import com.fourati.dto.response.LoanNotificationResponse;
import com.fourati.mapper.LoanNotificationMapper;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanNotificationRepository;
import com.fourati.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanNotificationServiceTest {

    @Mock
    private LoanNotificationRepository loanNotificationRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private LoanNotificationMapper loanNotificationMapper;

    @InjectMocks
    private LoanNotificationService loanNotificationService;

    @Test
    void create_rendersNotificationAndLinksItToLoanApplication() {
        UUID loanApplicationId = UUID.randomUUID();
        CreateLoanNotificationRequest request = new CreateLoanNotificationRequest(
                loanApplicationId, "application_submitted", "Application submitted",
                "Your loan application has been submitted.", "email");

        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanNotificationRepository.save(any(LoanNotification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanNotificationMapper.toResponse(any(LoanNotification.class))).thenReturn(
                new LoanNotificationResponse(UUID.randomUUID(), loanApplicationId, UUID.randomUUID(),
                        "application_submitted", "Application submitted",
                        "Your loan application has been submitted.", "email", null));

        LoanNotificationResponse response = loanNotificationService.create(request);

        assertThat(response.eventType()).isEqualTo("application_submitted");
        assertThat(response.title()).isEqualTo("Application submitted");
        verify(notificationRepository).save(any(Notification.class));
        verify(loanNotificationRepository).save(any(LoanNotification.class));
    }

    @Test
    void create_throwsNotFound_whenLoanApplicationMissing() {
        UUID loanApplicationId = UUID.randomUUID();
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanNotificationService.create(
                new CreateLoanNotificationRequest(loanApplicationId, "application_submitted", "title", "body", "email")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
