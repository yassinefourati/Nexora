package com.fourati.service;

import com.fourati.domain.AppModule;
import com.fourati.dto.request.CreateAppModuleRequest;
import com.fourati.dto.request.UpdateAppModuleRequest;
import com.fourati.dto.response.AppModuleResponse;
import com.fourati.mapper.AppModuleMapper;
import com.fourati.repository.AppModuleRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AppModuleService {

    private final AppModuleRepository appModuleRepository;
    private final AppModuleMapper appModuleMapper;

    @Audited(action = "CREATE", description = "Create a new application module")
    public AppModuleResponse create(CreateAppModuleRequest request) {
        if (appModuleRepository.existsByKey(request.key())) {
            throw new ConflictException("App module already exists with key: " + request.key());
        }
        AppModule entity = appModuleMapper.toEntity(request);
        AppModule saved = appModuleRepository.save(entity);
        return appModuleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AppModuleResponse findById(UUID id) {
        AppModule entity = getEntityOrThrow(id);
        return appModuleMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<AppModuleResponse> findAll(Pageable pageable) {
        return appModuleRepository.findAll(pageable).map(appModuleMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AppModuleResponse> findActive() {
        return appModuleRepository.findByActiveTrue().stream()
                .map(appModuleMapper::toResponse)
                .toList();
    }

    @Audited(action = "UPDATE", description = "Update an application module")
    public AppModuleResponse update(UUID id, UpdateAppModuleRequest request) {
        AppModule entity = getEntityOrThrow(id);
        appModuleMapper.updateEntityFromRequest(request, entity);
        AppModule saved = appModuleRepository.save(entity);
        return appModuleMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Delete an application module")
    public void delete(UUID id) {
        AppModule entity = getEntityOrThrow(id);
        appModuleRepository.delete(entity);
    }

    private AppModule getEntityOrThrow(UUID id) {
        return appModuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppModule", id));
    }
}
