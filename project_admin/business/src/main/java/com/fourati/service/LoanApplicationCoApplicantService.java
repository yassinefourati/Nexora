package com.fourati.service;

import com.fourati.domain.Customer;
import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanApplicationCoApplicant;
import com.fourati.dto.request.CreateLoanApplicationCoApplicantRequest;
import com.fourati.dto.response.LoanApplicationCoApplicantResponse;
import com.fourati.mapper.LoanApplicationCoApplicantMapper;
import com.fourati.repository.CustomerRepository;
import com.fourati.repository.LoanApplicationCoApplicantRepository;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanApplicationCoApplicantService {

    private final LoanApplicationCoApplicantRepository loanApplicationCoApplicantRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final LoanApplicationCoApplicantMapper loanApplicationCoApplicantMapper;

    @Audited(action = "CREATE", description = "Add a co-applicant to a loan application")
    public LoanApplicationCoApplicantResponse create(CreateLoanApplicationCoApplicantRequest request) {
        if (loanApplicationCoApplicantRepository.existsByLoanApplicationIdAndCustomerId(
                request.loanApplicationId(), request.customerId())) {
            throw new ConflictException("Customer " + request.customerId()
                    + " is already a co-applicant on loan application " + request.loanApplicationId());
        }
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));

        LoanApplicationCoApplicant entity = loanApplicationCoApplicantMapper.toEntity(request);
        entity.setLoanApplication(loanApplication);
        entity.setCustomer(customer);
        LoanApplicationCoApplicant saved = loanApplicationCoApplicantRepository.save(entity);
        return loanApplicationCoApplicantMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationCoApplicantResponse> findByLoanApplicationId(UUID loanApplicationId) {
        return loanApplicationCoApplicantRepository.findByLoanApplicationId(loanApplicationId).stream()
                .map(loanApplicationCoApplicantMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove a co-applicant from a loan application")
    public void delete(UUID id) {
        LoanApplicationCoApplicant entity = loanApplicationCoApplicantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplicationCoApplicant", id));
        loanApplicationCoApplicantRepository.delete(entity);
    }
}
