package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanContract;
import com.fourati.domain.LoanOffer;
import com.fourati.dto.request.CancelLoanContractRequest;
import com.fourati.dto.request.CreateLoanContractRequest;
import com.fourati.dto.request.FinalizeLoanContractRequest;
import com.fourati.dto.response.LoanContractResponse;
import com.fourati.mapper.LoanContractMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanContractRepository;
import com.fourati.repository.LoanContractStatusHistoryRepository;
import com.fourati.repository.LoanOfferRepository;
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
class LoanContractServiceTest {

    @Mock
    private LoanContractRepository loanContractRepository;

    @Mock
    private LoanContractStatusHistoryRepository loanContractStatusHistoryRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanOfferRepository loanOfferRepository;

    @Mock
    private LoanContractMapper loanContractMapper;

    @InjectMocks
    private LoanContractService loanContractService;

    @Test
    void create_copiesOfferedTermsAndRecordsStatusHistory() {
        UUID loanApplicationId = UUID.randomUUID();
        UUID loanOfferId = UUID.randomUUID();
        CreateLoanContractRequest request = new CreateLoanContractRequest(loanApplicationId, loanOfferId, "CTR-0001");

        LoanOffer offer = new LoanOffer();
        offer.setStatus("accepted");
        offer.setOfferedAmount(BigDecimal.valueOf(20000));
        offer.setOfferedTermMonths(48);
        offer.setInterestRate(BigDecimal.valueOf(5.5));
        LoanContract entity = new LoanContract();

        when(loanContractRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanContractRepository.existsByContractNumber("CTR-0001")).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(loanOfferRepository.findById(loanOfferId)).thenReturn(Optional.of(offer));
        when(loanContractMapper.toEntity(request)).thenReturn(entity);
        when(loanContractRepository.save(any(LoanContract.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanContractMapper.toResponse(any(LoanContract.class))).thenReturn(
                new LoanContractResponse(UUID.randomUUID(), loanApplicationId, loanOfferId, "CTR-0001", "draft",
                        BigDecimal.valueOf(20000), 48, BigDecimal.valueOf(5.5), null, null, null, null, null, null));

        LoanContractResponse response = loanContractService.create(request);

        assertThat(response.status()).isEqualTo("draft");
        assertThat(entity.getPrincipalAmount()).isEqualTo(BigDecimal.valueOf(20000));
        assertThat(entity.getTermMonths()).isEqualTo(48);
        verify(loanContractStatusHistoryRepository).save(any());
    }

    @Test
    void create_throwsConflict_whenOfferNotAccepted() {
        UUID loanApplicationId = UUID.randomUUID();
        UUID loanOfferId = UUID.randomUUID();
        LoanOffer offer = new LoanOffer();
        offer.setStatus("issued");

        when(loanContractRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanContractRepository.existsByContractNumber("CTR-0002")).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(loanOfferRepository.findById(loanOfferId)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> loanContractService.create(
                new CreateLoanContractRequest(loanApplicationId, loanOfferId, "CTR-0002")))
                .isInstanceOf(ConflictException.class);

        verify(loanContractRepository, never()).save(any());
    }

    @Test
    void create_throwsConflict_whenContractNumberAlreadyUsed() {
        when(loanContractRepository.existsByLoanApplicationId(any())).thenReturn(false);
        when(loanContractRepository.existsByContractNumber("CTR-0003")).thenReturn(true);

        assertThatThrownBy(() -> loanContractService.create(
                new CreateLoanContractRequest(UUID.randomUUID(), UUID.randomUUID(), "CTR-0003")))
                .isInstanceOf(ConflictException.class);

        verify(loanContractRepository, never()).save(any());
    }

    @Test
    void finalizeContract_transitionsDraftToFinalizedAndRecordsHistory() {
        UUID id = UUID.randomUUID();
        LoanContract entity = new LoanContract();
        entity.setStatus("draft");

        when(loanContractRepository.findById(id)).thenReturn(Optional.of(entity));
        when(loanContractRepository.save(any(LoanContract.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanContractMapper.toResponse(any(LoanContract.class))).thenReturn(
                new LoanContractResponse(id, null, null, "CTR-0001", "finalized", null, 0, null, "https://docs/ctr-0001.pdf", null, null, null, null, null));

        LoanContractResponse response = loanContractService.finalizeContract(id, new FinalizeLoanContractRequest("https://docs/ctr-0001.pdf"));

        assertThat(response.status()).isEqualTo("finalized");
        assertThat(entity.getFinalizedAt()).isNotNull();
        verify(loanContractStatusHistoryRepository).save(any());
    }

    @Test
    void cancel_throwsConflict_whenAlreadyCancelled() {
        UUID id = UUID.randomUUID();
        LoanContract entity = new LoanContract();
        entity.setStatus("cancelled");
        when(loanContractRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> loanContractService.cancel(id, new CancelLoanContractRequest("duplicate")))
                .isInstanceOf(ConflictException.class);

        verify(loanContractRepository, never()).save(any());
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(loanContractRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanContractService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
