package com.fourati.service;

import com.fourati.domain.SystemEvent;
import com.fourati.dto.request.CreateSystemEventRequest;
import com.fourati.dto.response.SystemEventResponse;
import com.fourati.mapper.SystemEventMapper;
import com.fourati.repository.SystemEventRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for system event entries.
 *
 * system_events is a read-only-from-the-API resource: entries are written
 * internally by the application (via {@link #record(CreateSystemEventRequest)})
 * whenever a notable system/application event occurs. No public
 * update/delete operations are exposed.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SystemEventService {

    private final SystemEventRepository systemEventRepository;
    private final SystemEventMapper systemEventMapper;

    @Audited(action = "CREATE", description = "Recorded a new system event")
    public SystemEventResponse record(CreateSystemEventRequest request) {
        SystemEvent systemEvent = systemEventMapper.toEntity(request);
        SystemEvent saved = systemEventRepository.save(systemEvent);
        return systemEventMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SystemEventResponse findById(UUID id) {
        SystemEvent systemEvent = systemEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemEvent", id));
        return systemEventMapper.toResponse(systemEvent);
    }

    @Transactional(readOnly = true)
    public Page<SystemEventResponse> findAll(Pageable pageable) {
        return systemEventRepository.findAll(pageable).map(systemEventMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SystemEventResponse> findByEventType(String eventType, Pageable pageable) {
        return systemEventRepository.findByEventType(eventType, pageable).map(systemEventMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SystemEventResponse> findBySeverity(String severity, Pageable pageable) {
        return systemEventRepository.findBySeverity(severity, pageable).map(systemEventMapper::toResponse);
    }
}
