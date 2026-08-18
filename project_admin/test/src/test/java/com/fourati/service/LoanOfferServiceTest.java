package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanApproval;
import com.fourati.domain.LoanOffer;
import com.fourati.dto.request.CreateLoanOfferRequest;
import com.fourati.dto.request.DeclineLoanOfferRequest;
import com.fourati.dto.response.LoanOfferResponse;
import com.fourati.mapper.LoanOfferMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanApprovalRepository;
import com.fourati.repository.LoanOfferRepository;
import com.fourati.repository.LoanOfferStatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanOfferServiceTest {

    @Mock
    private LoanOfferRepository loanOfferRepository;

    @Mock
    private LoanOfferStatusHistoryRepository loanOfferStatusHistoryRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanApprovalRepository loanApprovalRepository;

    @Mock
    private LoanOfferMapper loanOfferMapper;

    @InjectMocks
    private LoanOfferService loanOfferService;

    @Test
    void create_copiesApprovedTermsAndRecordsStatusHistory() {
        UUID loanApplicationId = UUID.randomUUID();
        UUID loanApprovalId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
        CreateLoanOfferRequest request = new CreateLoanOfferRequest(loanApplicationId, loanApprovalId, expiresAt);

        LoanApproval approval = new LoanApproval();
        approval.setStatus("approved");
        approval.setApprovedAmount(BigDecimal.valueOf(20000));
        approval.setApprovedTermMonths(48);
        approval.setInterestRate(BigDecimal.valueOf(5.5));
        LoanOffer entity = new LoanOffer();

        when(loanOfferRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(loanApprovalRepository.findById(loanApprovalId)).thenReturn(Optional.of(approval));
        when(loanOfferMapper.toEntity(request)).thenReturn(entity);
        when(loanOfferRepository.save(any(LoanOffer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanOfferMapper.toResponse(any(LoanOffer.class))).thenReturn(
                new LoanOfferResponse(UUID.randomUUID(), loanApplicationId, loanApprovalId, "issued",
                        BigDecimal.valueOf(20000), 48, BigDecimal.valueOf(5.5), null, null, expiresAt, null, null, null, null));

        LoanOfferResponse response = loanOfferService.create(request);

        assertThat(response.status()).isEqualTo("issued");
        assertThat(entity.getOfferedAmount()).isEqualTo(BigDecimal.valueOf(20000));
        assertThat(entity.getOfferedTermMonths()).isEqualTo(48);
        verify(loanOfferStatusHistoryRepository).save(any());
    }

    @Test
    void create_throwsConflict_whenApprovalNotApproved() {
        UUID loanApplicationId = UUID.randomUUID();
        UUID loanApprovalId = UUID.randomUUID();
        LoanApproval approval = new LoanApproval();
        approval.setStatus("pending");

        when(loanOfferRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(loanApprovalRepository.findById(loanApprovalId)).thenReturn(Optional.of(approval));

        assertThatThrownBy(() -> loanOfferService.create(
                new CreateLoanOfferRequest(loanApplicationId, loanApprovalId, Instant.now().plus(30, ChronoUnit.DAYS))))
                .isInstanceOf(ConflictException.class);

        verify(loanOfferRepository, never()).save(any());
    }

    @Test
    void accept_transitionsIssuedToAcceptedAndRecordsHistory() {
        UUID id = UUID.randomUUID();
        LoanOffer entity = new LoanOffer();
        entity.setStatus("issued");

        when(loanOfferRepository.findById(id)).thenReturn(Optional.of(entity));
        when(loanOfferRepository.save(any(LoanOffer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanOfferMapper.toResponse(any(LoanOffer.class))).thenReturn(
                new LoanOfferResponse(id, null, null, "accepted", null, 0, null, null, null, null, null, null, null, null));

        LoanOfferResponse response = loanOfferService.accept(id);

        assertThat(response.status()).isEqualTo("accepted");
        assertThat(entity.getAcceptedAt()).isNotNull();
        verify(loanOfferStatusHistoryRepository).save(any());
    }

    @Test
    void decline_throwsConflict_whenNotIssued() {
        UUID id = UUID.randomUUID();
        LoanOffer entity = new LoanOffer();
        entity.setStatus("accepted");
        when(loanOfferRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> loanOfferService.decline(id, new DeclineLoanOfferRequest("changed mind")))
                .isInstanceOf(ConflictException.class);

        verify(loanOfferRepository, never()).save(any());
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(loanOfferRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanOfferService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
