package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanApproval;
import com.fourati.domain.UnderwritingCase;
import com.fourati.dto.request.ApproveLoanApprovalRequest;
import com.fourati.dto.request.CreateLoanApprovalRequest;
import com.fourati.dto.request.RejectLoanApprovalRequest;
import com.fourati.dto.response.LoanApprovalResponse;
import com.fourati.mapper.LoanApprovalMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanApprovalRepository;
import com.fourati.repository.LoanApprovalStatusHistoryRepository;
import com.fourati.repository.UnderwritingCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApprovalServiceTest {

    @Mock
    private LoanApprovalRepository loanApprovalRepository;

    @Mock
    private LoanApprovalStatusHistoryRepository loanApprovalStatusHistoryRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private UnderwritingCaseRepository underwritingCaseRepository;

    @Mock
    private LoanApprovalMapper loanApprovalMapper;

    @InjectMocks
    private LoanApprovalService loanApprovalService;

    @Test
    void create_savesApprovalInPendingAndRecordsStatusHistory() {
        UUID loanApplicationId = UUID.randomUUID();
        UUID underwritingCaseId = UUID.randomUUID();
        CreateLoanApprovalRequest request = new CreateLoanApprovalRequest(loanApplicationId, underwritingCaseId);
        UnderwritingCase underwritingCase = new UnderwritingCase();
        underwritingCase.setStatus("completed");
        LoanApproval entity = new LoanApproval();

        when(loanApprovalRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(underwritingCaseRepository.findById(underwritingCaseId)).thenReturn(Optional.of(underwritingCase));
        when(loanApprovalMapper.toEntity(request)).thenReturn(entity);
        when(loanApprovalRepository.save(any(LoanApproval.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanApprovalMapper.toResponse(any(LoanApproval.class))).thenReturn(
                new LoanApprovalResponse(UUID.randomUUID(), loanApplicationId, underwritingCaseId, "pending",
                        null, null, null, null, null, null, null, null, null));

        LoanApprovalResponse response = loanApprovalService.create(request);

        assertThat(response.status()).isEqualTo("pending");
        verify(loanApprovalStatusHistoryRepository).save(any());
    }

    @Test
    void create_throwsConflict_whenUnderwritingCaseNotCompleted() {
        UUID loanApplicationId = UUID.randomUUID();
        UUID underwritingCaseId = UUID.randomUUID();
        UnderwritingCase underwritingCase = new UnderwritingCase();
        underwritingCase.setStatus("in_review");

        when(loanApprovalRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(underwritingCaseRepository.findById(underwritingCaseId)).thenReturn(Optional.of(underwritingCase));

        assertThatThrownBy(() -> loanApprovalService.create(new CreateLoanApprovalRequest(loanApplicationId, underwritingCaseId)))
                .isInstanceOf(ConflictException.class);

        verify(loanApprovalRepository, never()).save(any());
    }

    @Test
    void create_throwsConflict_whenApprovalAlreadyExistsForApplication() {
        UUID loanApplicationId = UUID.randomUUID();
        when(loanApprovalRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(true);

        assertThatThrownBy(() -> loanApprovalService.create(new CreateLoanApprovalRequest(loanApplicationId, UUID.randomUUID())))
                .isInstanceOf(ConflictException.class);

        verify(loanApprovalRepository, never()).save(any());
    }

    @Test
    void approve_transitionsPendingToApprovedAndRecordsHistory() {
        UUID id = UUID.randomUUID();
        LoanApproval entity = new LoanApproval();
        entity.setStatus("pending");
        ApproveLoanApprovalRequest request = new ApproveLoanApprovalRequest(
                BigDecimal.valueOf(20000), 48, BigDecimal.valueOf(5.5), "john.approver");

        when(loanApprovalRepository.findById(id)).thenReturn(Optional.of(entity));
        when(loanApprovalRepository.save(any(LoanApproval.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanApprovalMapper.toResponse(any(LoanApproval.class))).thenReturn(
                new LoanApprovalResponse(id, null, null, "approved", BigDecimal.valueOf(20000), 48,
                        BigDecimal.valueOf(5.5), "john.approver", null, null, null, null, null));

        LoanApprovalResponse response = loanApprovalService.approve(id, request);

        assertThat(response.status()).isEqualTo("approved");
        assertThat(entity.getApprovedAt()).isNotNull();
        verify(loanApprovalStatusHistoryRepository).save(any());
    }

    @Test
    void approve_throwsConflict_whenNotPending() {
        UUID id = UUID.randomUUID();
        LoanApproval entity = new LoanApproval();
        entity.setStatus("approved");
        when(loanApprovalRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> loanApprovalService.approve(id,
                new ApproveLoanApprovalRequest(BigDecimal.TEN, 12, BigDecimal.ONE, "someone")))
                .isInstanceOf(ConflictException.class);

        verify(loanApprovalRepository, never()).save(any());
    }

    @Test
    void reject_transitionsPendingToRejectedAndRecordsHistory() {
        UUID id = UUID.randomUUID();
        LoanApproval entity = new LoanApproval();
        entity.setStatus("pending");

        when(loanApprovalRepository.findById(id)).thenReturn(Optional.of(entity));
        when(loanApprovalRepository.save(any(LoanApproval.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanApprovalMapper.toResponse(any(LoanApproval.class))).thenReturn(
                new LoanApprovalResponse(id, null, null, "rejected", null, null, null, null,
                        "Insufficient collateral", null, null, null, null));

        LoanApprovalResponse response = loanApprovalService.reject(id, new RejectLoanApprovalRequest("Insufficient collateral"));

        assertThat(response.status()).isEqualTo("rejected");
        verify(loanApprovalStatusHistoryRepository).save(any());
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(loanApprovalRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanApprovalService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
