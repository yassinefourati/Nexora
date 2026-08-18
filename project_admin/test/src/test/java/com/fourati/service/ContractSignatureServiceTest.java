package com.fourati.service;

import com.fourati.domain.ContractSignature;
import com.fourati.domain.LoanContract;
import com.fourati.dto.request.CreateContractSignatureRequest;
import com.fourati.dto.request.DeclineContractSignatureRequest;
import com.fourati.dto.response.ContractSignatureResponse;
import com.fourati.mapper.ContractSignatureMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.ContractSignatureRepository;
import com.fourati.repository.ContractSignatureStatusHistoryRepository;
import com.fourati.repository.LoanContractRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractSignatureServiceTest {

    @Mock
    private ContractSignatureRepository contractSignatureRepository;

    @Mock
    private ContractSignatureStatusHistoryRepository contractSignatureStatusHistoryRepository;

    @Mock
    private LoanContractRepository loanContractRepository;

    @Mock
    private ContractSignatureMapper contractSignatureMapper;

    @InjectMocks
    private ContractSignatureService contractSignatureService;

    @Test
    void create_savesSignatureInPendingAndRecordsStatusHistory() {
        UUID loanContractId = UUID.randomUUID();
        CreateContractSignatureRequest request = new CreateContractSignatureRequest(
                loanContractId, "Jane Doe", "primary_applicant", "electronic");
        LoanContract contract = new LoanContract();
        contract.setStatus("finalized");
        ContractSignature entity = new ContractSignature();

        when(loanContractRepository.findById(loanContractId)).thenReturn(Optional.of(contract));
        when(contractSignatureMapper.toEntity(request)).thenReturn(entity);
        when(contractSignatureRepository.save(any(ContractSignature.class))).thenAnswer(inv -> inv.getArgument(0));
        when(contractSignatureMapper.toResponse(any(ContractSignature.class))).thenReturn(
                new ContractSignatureResponse(UUID.randomUUID(), loanContractId, "Jane Doe", "primary_applicant",
                        "pending", "electronic", null, null, null, null, null, null));

        ContractSignatureResponse response = contractSignatureService.create(request);

        assertThat(response.status()).isEqualTo("pending");
        verify(contractSignatureStatusHistoryRepository).save(any());
    }

    @Test
    void create_throwsConflict_whenContractNotFinalized() {
        UUID loanContractId = UUID.randomUUID();
        LoanContract contract = new LoanContract();
        contract.setStatus("draft");
        when(loanContractRepository.findById(loanContractId)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> contractSignatureService.create(
                new CreateContractSignatureRequest(loanContractId, "Jane Doe", "primary_applicant", null)))
                .isInstanceOf(ConflictException.class);

        verify(contractSignatureRepository, never()).save(any());
    }

    @Test
    void sign_transitionsPendingToSignedAndRecordsHistory() {
        UUID id = UUID.randomUUID();
        ContractSignature entity = new ContractSignature();
        entity.setStatus("pending");
        entity.setSignerName("Jane Doe");

        when(contractSignatureRepository.findById(id)).thenReturn(Optional.of(entity));
        when(contractSignatureRepository.save(any(ContractSignature.class))).thenAnswer(inv -> inv.getArgument(0));
        when(contractSignatureMapper.toResponse(any(ContractSignature.class))).thenReturn(
                new ContractSignatureResponse(id, null, "Jane Doe", "primary_applicant", "signed", "electronic",
                        null, null, null, null, null, null));

        ContractSignatureResponse response = contractSignatureService.sign(id);

        assertThat(response.status()).isEqualTo("signed");
        assertThat(entity.getSignedAt()).isNotNull();
        verify(contractSignatureStatusHistoryRepository).save(any());
    }

    @Test
    void decline_throwsConflict_whenNotPending() {
        UUID id = UUID.randomUUID();
        ContractSignature entity = new ContractSignature();
        entity.setStatus("signed");
        when(contractSignatureRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> contractSignatureService.decline(id, new DeclineContractSignatureRequest("changed mind")))
                .isInstanceOf(ConflictException.class);

        verify(contractSignatureRepository, never()).save(any());
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(contractSignatureRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractSignatureService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
