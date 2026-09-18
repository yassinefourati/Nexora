package com.fourati.service;

import com.fourati.domain.FraudAlert;
import com.fourati.dto.response.FraudAlertResponse;
import com.fourati.mapper.FraudAlertMapper;
import com.fourati.repository.FraudAlertRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Alerts are raised internally by {@link FraudCheckService#process}; this
 * service only lists and resolves them.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FraudAlertService {

    private final FraudAlertRepository fraudAlertRepository;
    private final FraudAlertMapper fraudAlertMapper;

    @Transactional(readOnly = true)
    public List<FraudAlertResponse> findByFraudCheckId(UUID fraudCheckId) {
        return fraudAlertRepository.findByFraudCheckId(fraudCheckId).stream()
                .map(fraudAlertMapper::toResponse)
                .toList();
    }

    @Audited(action = "RESOLVE", description = "Resolve a fraud alert")
    public FraudAlertResponse resolve(UUID id) {
        FraudAlert entity = fraudAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FraudAlert", id));
        if ("resolved".equals(entity.getStatus()) || "dismissed".equals(entity.getStatus())) {
            throw new ConflictException("Fraud alert " + id + " is already closed with status: " + entity.getStatus());
        }
        entity.setStatus("resolved");
        entity.setResolvedAt(Instant.now());
        FraudAlert saved = fraudAlertRepository.save(entity);
        return fraudAlertMapper.toResponse(saved);
    }
}
