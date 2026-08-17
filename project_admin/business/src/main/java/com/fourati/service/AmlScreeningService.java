package com.fourati.service;

import com.fourati.domain.AmlScreening;
import com.fourati.domain.KycCase;
import com.fourati.dto.request.CreateAmlScreeningRequest;
import com.fourati.dto.response.AmlScreeningResponse;
import com.fourati.mapper.AmlScreeningMapper;
import com.fourati.repository.AmlScreeningRepository;
import com.fourati.repository.KycCaseRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AmlScreeningService {

    private final AmlScreeningRepository amlScreeningRepository;
    private final KycCaseRepository kycCaseRepository;
    private final AmlScreeningMapper amlScreeningMapper;

    @Audited(action = "CREATE", description = "Record an AML screening result")
    public AmlScreeningResponse create(CreateAmlScreeningRequest request) {
        KycCase kycCase = kycCaseRepository.findById(request.kycCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("KycCase", request.kycCaseId()));
        AmlScreening entity = amlScreeningMapper.toEntity(request);
        entity.setKycCase(kycCase);
        AmlScreening saved = amlScreeningRepository.save(entity);
        return amlScreeningMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AmlScreeningResponse> findByKycCaseId(UUID kycCaseId) {
        return amlScreeningRepository.findByKycCaseId(kycCaseId).stream()
                .map(amlScreeningMapper::toResponse)
                .toList();
    }
}
