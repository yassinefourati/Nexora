package com.fourati.service;

import com.fourati.domain.AmlAlert;
import com.fourati.domain.AmlScreening;
import com.fourati.dto.request.CreateAmlAlertRequest;
import com.fourati.dto.response.AmlAlertResponse;
import com.fourati.mapper.AmlAlertMapper;
import com.fourati.repository.AmlAlertRepository;
import com.fourati.repository.AmlScreeningRepository;
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
public class AmlAlertService {

    private final AmlAlertRepository amlAlertRepository;
    private final AmlScreeningRepository amlScreeningRepository;
    private final AmlAlertMapper amlAlertMapper;

    @Audited(action = "CREATE", description = "Raise an AML alert")
    public AmlAlertResponse create(CreateAmlAlertRequest request) {
        AmlScreening amlScreening = amlScreeningRepository.findById(request.amlScreeningId())
                .orElseThrow(() -> new ResourceNotFoundException("AmlScreening", request.amlScreeningId()));
        AmlAlert entity = amlAlertMapper.toEntity(request);
        entity.setAmlScreening(amlScreening);
        AmlAlert saved = amlAlertRepository.save(entity);
        return amlAlertMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AmlAlertResponse> findByAmlScreeningId(UUID amlScreeningId) {
        return amlAlertRepository.findByAmlScreeningId(amlScreeningId).stream()
                .map(amlAlertMapper::toResponse)
                .toList();
    }

    @Audited(action = "RESOLVE", description = "Resolve an AML alert")
    public AmlAlertResponse resolve(UUID id) {
        AmlAlert entity = amlAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AmlAlert", id));
        if ("resolved".equals(entity.getStatus()) || "dismissed".equals(entity.getStatus())) {
            throw new ConflictException("AML alert " + id + " is already closed with status: " + entity.getStatus());
        }
        entity.setStatus("resolved");
        entity.setResolvedAt(Instant.now());
        AmlAlert saved = amlAlertRepository.save(entity);
        return amlAlertMapper.toResponse(saved);
    }
}
