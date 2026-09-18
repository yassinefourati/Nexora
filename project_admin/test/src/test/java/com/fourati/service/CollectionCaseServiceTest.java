package com.fourati.service;

import com.fourati.domain.CollectionCase;
import com.fourati.domain.LoanAccount;
import com.fourati.domain.LoanInstallment;
import com.fourati.dto.request.CreateCollectionCaseRequest;
import com.fourati.dto.request.EscalateCollectionCaseRequest;
import com.fourati.dto.request.ResolveCollectionCaseRequest;
import com.fourati.dto.request.WriteOffCollectionCaseRequest;
import com.fourati.dto.response.CollectionCaseResponse;
import com.fourati.mapper.CollectionCaseMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.CollectionCaseRepository;
import com.fourati.repository.CollectionCaseStatusHistoryRepository;
import com.fourati.repository.LoanAccountRepository;
import com.fourati.repository.LoanInstallmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionCaseServiceTest {

    @Mock
    private CollectionCaseRepository collectionCaseRepository;

    @Mock
    private CollectionCaseStatusHistoryRepository collectionCaseStatusHistoryRepository;

    @Mock
    private LoanAccountRepository loanAccountRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @Mock
    private CollectionCaseMapper collectionCaseMapper;

    @InjectMocks
    private CollectionCaseService collectionCaseService;

    private LoanAccount accountWithId(UUID id) {
        LoanAccount account = new LoanAccount();
        account.setId(id);
        return account;
    }

    private LoanInstallment overdueInstallment(LoanAccount account) {
        LoanInstallment installment = new LoanInstallment();
        installment.setLoanAccount(account);
        installment.setStatus("pending");
        installment.setDueDate(LocalDate.now().minusDays(5));
        installment.setTotalAmount(BigDecimal.valueOf(1050));
        return installment;
    }

    @Test
    void create_savesCaseInOpenAndRecordsStatusHistory() {
        UUID loanAccountId = UUID.randomUUID();
        UUID loanInstallmentId = UUID.randomUUID();
        CreateCollectionCaseRequest request = new CreateCollectionCaseRequest(loanAccountId, loanInstallmentId, "agent.smith");

        LoanAccount account = accountWithId(loanAccountId);
        LoanInstallment installment = overdueInstallment(account);
        CollectionCase entity = new CollectionCase();

        when(collectionCaseRepository.existsByLoanInstallmentId(loanInstallmentId)).thenReturn(false);
        when(loanAccountRepository.findById(loanAccountId)).thenReturn(Optional.of(account));
        when(loanInstallmentRepository.findById(loanInstallmentId)).thenReturn(Optional.of(installment));
        when(collectionCaseMapper.toEntity(request)).thenReturn(entity);
        when(collectionCaseRepository.save(any(CollectionCase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(collectionCaseMapper.toResponse(any(CollectionCase.class))).thenReturn(
                new CollectionCaseResponse(UUID.randomUUID(), loanAccountId, loanInstallmentId, "open", "reminder",
                        "agent.smith", BigDecimal.valueOf(1050), null, null, null, null, null));

        CollectionCaseResponse response = collectionCaseService.create(request);

        assertThat(response.status()).isEqualTo("open");
        assertThat(entity.getOverdueAmount()).isEqualByComparingTo(BigDecimal.valueOf(1050));
        verify(collectionCaseStatusHistoryRepository).save(any());
    }

    @Test
    void create_throwsConflict_whenInstallmentNotYetOverdue() {
        UUID loanAccountId = UUID.randomUUID();
        UUID loanInstallmentId = UUID.randomUUID();
        LoanAccount account = accountWithId(loanAccountId);
        LoanInstallment installment = overdueInstallment(account);
        installment.setDueDate(LocalDate.now().plusDays(5));

        when(collectionCaseRepository.existsByLoanInstallmentId(loanInstallmentId)).thenReturn(false);
        when(loanAccountRepository.findById(loanAccountId)).thenReturn(Optional.of(account));
        when(loanInstallmentRepository.findById(loanInstallmentId)).thenReturn(Optional.of(installment));

        assertThatThrownBy(() -> collectionCaseService.create(
                new CreateCollectionCaseRequest(loanAccountId, loanInstallmentId, null)))
                .isInstanceOf(ConflictException.class);

        verify(collectionCaseRepository, never()).save(any());
    }

    @Test
    void escalate_movesToInProgressAndUpdatesStage() {
        UUID id = UUID.randomUUID();
        CollectionCase entity = new CollectionCase();
        entity.setStatus("open");
        entity.setStage("reminder");

        when(collectionCaseRepository.findById(id)).thenReturn(Optional.of(entity));
        when(collectionCaseRepository.save(any(CollectionCase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(collectionCaseMapper.toResponse(any(CollectionCase.class))).thenReturn(
                new CollectionCaseResponse(id, null, null, "in_progress", "notice", null, null, null, null, null, null, null));

        CollectionCaseResponse response = collectionCaseService.escalate(id, new EscalateCollectionCaseRequest("notice"));

        assertThat(response.status()).isEqualTo("in_progress");
        assertThat(entity.getStage()).isEqualTo("notice");
        verify(collectionCaseStatusHistoryRepository).save(any());
    }

    @Test
    void resolve_throwsConflict_whenAlreadyWrittenOff() {
        UUID id = UUID.randomUUID();
        CollectionCase entity = new CollectionCase();
        entity.setStatus("written_off");
        when(collectionCaseRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> collectionCaseService.resolve(id, new ResolveCollectionCaseRequest("paid overdue amount")))
                .isInstanceOf(ConflictException.class);

        verify(collectionCaseRepository, never()).save(any());
    }

    @Test
    void writeOff_transitionsOpenToWrittenOffAndRecordsHistory() {
        UUID id = UUID.randomUUID();
        CollectionCase entity = new CollectionCase();
        entity.setStatus("in_progress");

        when(collectionCaseRepository.findById(id)).thenReturn(Optional.of(entity));
        when(collectionCaseRepository.save(any(CollectionCase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(collectionCaseMapper.toResponse(any(CollectionCase.class))).thenReturn(
                new CollectionCaseResponse(id, null, null, "written_off", "agency", null, null,
                        "uncollectable", null, null, null, null));

        CollectionCaseResponse response = collectionCaseService.writeOff(id, new WriteOffCollectionCaseRequest("uncollectable"));

        assertThat(response.status()).isEqualTo("written_off");
        assertThat(entity.getResolvedAt()).isNotNull();
        verify(collectionCaseStatusHistoryRepository).save(any());
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(collectionCaseRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionCaseService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
