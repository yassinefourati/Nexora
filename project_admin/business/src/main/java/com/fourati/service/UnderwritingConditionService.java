package com.fourati.service;

import com.fourati.domain.UnderwritingCase;
import com.fourati.domain.UnderwritingCondition;
import com.fourati.dto.request.CreateUnderwritingConditionRequest;
import com.fourati.dto.response.UnderwritingConditionResponse;
import com.fourati.mapper.UnderwritingConditionMapper;
import com.fourati.repository.UnderwritingCaseRepository;
import com.fourati.repository.UnderwritingConditionRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UnderwritingConditionService {

    private final UnderwritingConditionRepository underwritingConditionRepository;
    private final UnderwritingCaseRepository underwritingCaseRepository;
    private final UnderwritingConditionMapper underwritingConditionMapper;

    @Audited(action = "CREATE", description = "Attach a condition to an underwriting case")
    public UnderwritingConditionResponse create(CreateUnderwritingConditionRequest request) {
        UnderwritingCase underwritingCase = underwritingCaseRepository.findById(request.underwritingCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("UnderwritingCase", request.underwritingCaseId()));

        UnderwritingCondition entity = underwritingConditionMapper.toEntity(request);
        entity.setUnderwritingCase(underwritingCase);
        UnderwritingCondition saved = underwritingConditionRepository.save(entity);
        return underwritingConditionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UnderwritingConditionResponse> findByUnderwritingCaseId(UUID underwritingCaseId) {
        return underwritingConditionRepository.findByUnderwritingCaseId(underwritingCaseId).stream()
                .map(underwritingConditionMapper::toResponse)
                .toList();
    }

    @Audited(action = "SATISFY", description = "Mark an underwriting condition as satisfied")
    public UnderwritingConditionResponse satisfy(UUID id) {
        UnderwritingCondition entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("Underwriting condition " + id + " must be pending to satisfy, was: " + entity.getStatus());
        }
        entity.setStatus("satisfied");
        entity.setSatisfiedAt(Instant.now());
        UnderwritingCondition saved = underwritingConditionRepository.save(entity);
        return underwritingConditionMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Remove a condition from an underwriting case")
    public void delete(UUID id) {
        UnderwritingCondition entity = getEntityOrThrow(id);
        underwritingConditionRepository.delete(entity);
    }

    private UnderwritingCondition getEntityOrThrow(UUID id) {
        return underwritingConditionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UnderwritingCondition", id));
    }
}
