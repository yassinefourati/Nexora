package com.fourati.service;

import com.fourati.domain.ContractSignature;
import com.fourati.domain.ContractSignatureStatusHistory;
import com.fourati.domain.LoanContract;
import com.fourati.dto.request.CreateContractSignatureRequest;
import com.fourati.dto.request.DeclineContractSignatureRequest;
import com.fourati.dto.response.ContractSignatureResponse;
import com.fourati.mapper.ContractSignatureMapper;
import com.fourati.repository.ContractSignatureRepository;
import com.fourati.repository.ContractSignatureStatusHistoryRepository;
import com.fourati.repository.LoanContractRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Signature requests are explicit-state today (no workflow engine yet);
 * every transition is recorded in {@link ContractSignatureStatusHistory} so
 * the trail survives a future migration to Camunda-driven orchestration. A
 * signature can only be requested against a finalized {@link LoanContract}.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ContractSignatureService {

    private final ContractSignatureRepository contractSignatureRepository;
    private final ContractSignatureStatusHistoryRepository contractSignatureStatusHistoryRepository;
    private final LoanContractRepository loanContractRepository;
    private final ContractSignatureMapper contractSignatureMapper;

    @Audited(action = "CREATE", description = "Request a signature on a loan contract")
    public ContractSignatureResponse create(CreateContractSignatureRequest request) {
        LoanContract loanContract = loanContractRepository.findById(request.loanContractId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanContract", request.loanContractId()));
        if (!"finalized".equals(loanContract.getStatus())) {
            throw new ConflictException("Loan contract " + request.loanContractId()
                    + " must be finalized before a signature can be requested");
        }

        ContractSignature entity = contractSignatureMapper.toEntity(request);
        entity.setLoanContract(loanContract);
        ContractSignature saved = contractSignatureRepository.save(entity);
        recordStatusChange(saved, null, "pending", "Signature requested");
        return contractSignatureMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ContractSignatureResponse findById(UUID id) {
        return contractSignatureMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ContractSignatureResponse> findAll(Pageable pageable) {
        return contractSignatureRepository.findAll(pageable).map(contractSignatureMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ContractSignatureResponse> findByLoanContractId(UUID loanContractId) {
        return contractSignatureRepository.findByLoanContractId(loanContractId).stream()
                .map(contractSignatureMapper::toResponse)
                .toList();
    }

    @Audited(action = "SIGN", description = "Record a signature as signed")
    public ContractSignatureResponse sign(UUID id) {
        ContractSignature entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("Contract signature " + id + " must be pending to sign, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("signed");
        entity.setSignedAt(Instant.now());
        ContractSignature saved = contractSignatureRepository.save(entity);
        recordStatusChange(saved, previousStatus, "signed", "Signed by " + entity.getSignerName());
        return contractSignatureMapper.toResponse(saved);
    }

    @Audited(action = "DECLINE", description = "Decline a signature request")
    public ContractSignatureResponse decline(UUID id, DeclineContractSignatureRequest request) {
        ContractSignature entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("Contract signature " + id + " must be pending to decline, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("declined");
        entity.setDeclineReason(request.declineReason());
        entity.setDeclinedAt(Instant.now());
        ContractSignature saved = contractSignatureRepository.save(entity);
        recordStatusChange(saved, previousStatus, "declined", request.declineReason());
        return contractSignatureMapper.toResponse(saved);
    }

    private void recordStatusChange(ContractSignature contractSignature, String fromStatus, String toStatus, String reason) {
        ContractSignatureStatusHistory history = new ContractSignatureStatusHistory();
        history.setContractSignature(contractSignature);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        contractSignatureStatusHistoryRepository.save(history);
    }

    private ContractSignature getEntityOrThrow(UUID id) {
        return contractSignatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContractSignature", id));
    }
}
