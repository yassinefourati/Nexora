package com.fourati.service;

import com.fourati.domain.ContractSignature;
import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanContract;
import com.fourati.domain.LoanDisbursement;
import com.fourati.dto.request.CompleteLoanDisbursementRequest;
import com.fourati.dto.request.CreateLoanDisbursementRequest;
import com.fourati.dto.request.FailLoanDisbursementRequest;
import com.fourati.dto.response.LoanDisbursementResponse;
import com.fourati.mapper.LoanDisbursementMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.ContractSignatureRepository;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanContractRepository;
import com.fourati.repository.LoanDisbursementRepository;
import com.fourati.repository.LoanDisbursementStatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanDisbursementServiceTest {

    @Mock
    private LoanDisbursementRepository loanDisbursementRepository;

    @Mock
    private LoanDisbursementStatusHistoryRepository loanDisbursementStatusHistoryRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanContractRepository loanContractRepository;

    @Mock
    private ContractSignatureRepository contractSignatureRepository;

    @Mock
    private LoanDisbursementMapper loanDisbursementMapper;

    @InjectMocks
    private LoanDisbursementService loanDisbursementService;

    private ContractSignature signedSignature() {
        ContractSignature signature = new ContractSignature();
        signature.setStatus("signed");
        return signature;
    }

    @Test
    void create_copiesPrincipalAmountAndRecordsStatusHistory() {
        UUID loanApplicationId = UUID.randomUUID();
        UUID loanContractId = UUID.randomUUID();
        CreateLoanDisbursementRequest request = new CreateLoanDisbursementRequest(
                loanApplicationId, loanContractId, "bank_transfer", "IBAN123456");

        LoanContract contract = new LoanContract();
        contract.setStatus("finalized");
        contract.setPrincipalAmount(BigDecimal.valueOf(20000));
        LoanDisbursement entity = new LoanDisbursement();

        when(loanDisbursementRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(loanContractRepository.findById(loanContractId)).thenReturn(Optional.of(contract));
        when(contractSignatureRepository.findByLoanContractId(loanContractId)).thenReturn(List.of(signedSignature()));
        when(loanDisbursementMapper.toEntity(request)).thenReturn(entity);
        when(loanDisbursementRepository.save(any(LoanDisbursement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanDisbursementMapper.toResponse(any(LoanDisbursement.class))).thenReturn(
                new LoanDisbursementResponse(UUID.randomUUID(), loanApplicationId, loanContractId, "pending",
                        BigDecimal.valueOf(20000), "bank_transfer", "IBAN123456", null, null, null, null, null, null, null));

        LoanDisbursementResponse response = loanDisbursementService.create(request);

        assertThat(response.status()).isEqualTo("pending");
        assertThat(entity.getAmount()).isEqualTo(BigDecimal.valueOf(20000));
        verify(loanDisbursementStatusHistoryRepository).save(any());
    }

    @Test
    void create_throwsConflict_whenSignaturesIncomplete() {
        UUID loanApplicationId = UUID.randomUUID();
        UUID loanContractId = UUID.randomUUID();
        LoanContract contract = new LoanContract();
        contract.setStatus("finalized");
        ContractSignature pendingSignature = new ContractSignature();
        pendingSignature.setStatus("pending");

        when(loanDisbursementRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(loanContractRepository.findById(loanContractId)).thenReturn(Optional.of(contract));
        when(contractSignatureRepository.findByLoanContractId(loanContractId)).thenReturn(List.of(pendingSignature));

        assertThatThrownBy(() -> loanDisbursementService.create(
                new CreateLoanDisbursementRequest(loanApplicationId, loanContractId, "bank_transfer", "IBAN123456")))
                .isInstanceOf(ConflictException.class);

        verify(loanDisbursementRepository, never()).save(any());
    }

    @Test
    void create_throwsConflict_whenContractNotFinalized() {
        UUID loanApplicationId = UUID.randomUUID();
        UUID loanContractId = UUID.randomUUID();
        LoanContract contract = new LoanContract();
        contract.setStatus("draft");

        when(loanDisbursementRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(loanContractRepository.findById(loanContractId)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> loanDisbursementService.create(
                new CreateLoanDisbursementRequest(loanApplicationId, loanContractId, "bank_transfer", "IBAN123456")))
                .isInstanceOf(ConflictException.class);

        verify(loanDisbursementRepository, never()).save(any());
    }

    @Test
    void initiateThenComplete_transitionsStatusCorrectly() {
        UUID id = UUID.randomUUID();
        LoanDisbursement entity = new LoanDisbursement();
        entity.setStatus("pending");

        when(loanDisbursementRepository.findById(id)).thenReturn(Optional.of(entity));
        when(loanDisbursementRepository.save(any(LoanDisbursement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanDisbursementMapper.toResponse(any(LoanDisbursement.class))).thenReturn(
                new LoanDisbursementResponse(id, null, null, "initiated", null, null, null, null, null, null, null, null, null, null));

        LoanDisbursementResponse initiated = loanDisbursementService.initiate(id);
        assertThat(initiated.status()).isEqualTo("initiated");
        assertThat(entity.getInitiatedAt()).isNotNull();

        when(loanDisbursementMapper.toResponse(any(LoanDisbursement.class))).thenReturn(
                new LoanDisbursementResponse(id, null, null, "completed", null, null, null, "REF-1", null, null, null, null, null, null));

        LoanDisbursementResponse completed = loanDisbursementService.complete(id, new CompleteLoanDisbursementRequest("REF-1"));
        assertThat(completed.status()).isEqualTo("completed");
        assertThat(entity.getCompletedAt()).isNotNull();

        verify(loanDisbursementStatusHistoryRepository, org.mockito.Mockito.times(2)).save(any());
    }

    @Test
    void fail_throwsConflict_whenNotInitiated() {
        UUID id = UUID.randomUUID();
        LoanDisbursement entity = new LoanDisbursement();
        entity.setStatus("pending");
        when(loanDisbursementRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> loanDisbursementService.fail(id, new FailLoanDisbursementRequest("bank rejected")))
                .isInstanceOf(ConflictException.class);

        verify(loanDisbursementRepository, never()).save(any());
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(loanDisbursementRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanDisbursementService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
