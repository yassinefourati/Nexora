package com.fourati.service;

import com.fourati.domain.KycCase;
import com.fourati.domain.KycCheck;
import com.fourati.dto.request.CreateKycCheckRequest;
import com.fourati.dto.response.KycCheckResponse;
import com.fourati.mapper.KycCheckMapper;
import com.fourati.repository.KycCaseRepository;
import com.fourati.repository.KycCheckRepository;
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
public class KycCheckService {

    private final KycCheckRepository kycCheckRepository;
    private final KycCaseRepository kycCaseRepository;
    private final KycCheckMapper kycCheckMapper;

    @Audited(action = "CREATE", description = "Record a KYC check result")
    public KycCheckResponse create(CreateKycCheckRequest request) {
        KycCase kycCase = kycCaseRepository.findById(request.kycCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("KycCase", request.kycCaseId()));
        KycCheck entity = kycCheckMapper.toEntity(request);
        entity.setKycCase(kycCase);
        KycCheck saved = kycCheckRepository.save(entity);
        return kycCheckMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<KycCheckResponse> findByKycCaseId(UUID kycCaseId) {
        return kycCheckRepository.findByKycCaseId(kycCaseId).stream()
                .map(kycCheckMapper::toResponse)
                .toList();
    }
}
