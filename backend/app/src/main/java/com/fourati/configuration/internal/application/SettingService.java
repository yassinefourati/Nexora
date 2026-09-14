package com.fourati.configuration.internal.application;

import com.fourati.shared.persistence.TextSearch;

import com.fourati.organization.OrganizationFacade;

import com.fourati.shared.persistence.VersionCheck;

import com.fourati.configuration.internal.domain.Setting;
import com.fourati.configuration.CreateSettingRequest;
import com.fourati.configuration.UpdateSettingRequest;
import com.fourati.configuration.SettingResponse;
import com.fourati.platform.error.BusinessRuleException;
import com.fourati.platform.error.ErrorCode;
import com.fourati.configuration.internal.persistence.SettingRepository;
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
public class SettingService {

    private final SettingRepository settingRepository;
    private final OrganizationFacade organizations;
    private final SettingMapper settingMapper;

    @Audited(action = "CREATE", description = "Create a new setting")
    public SettingResponse create(CreateSettingRequest request) {
        boolean exists = request.organizationId() == null
                ? settingRepository.existsByKeyAndScopeAndOrganizationIdIsNull(request.key(), request.scope())
                : settingRepository.existsByKeyAndScopeAndOrganizationId(request.key(), request.scope(), request.organizationId());
        if (exists) {
            throw new ConflictException("Setting already exists with key: " + request.key()
                    + " and scope: " + request.scope());
        }
        Setting entity = settingMapper.toEntity(request);
        if (request.organizationId() != null) {
            organizations.ensureExists(request.organizationId());
            entity.setOrganizationId(request.organizationId());
        }
        Setting saved = settingRepository.save(entity);
        return settingMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SettingResponse findById(UUID id) {
        return settingMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<SettingResponse> findAll(Pageable pageable) {
        return settingRepository.findAll(pageable).map(settingMapper::toResponse);
    }

    /** Server-side search over key, description (case-insensitive contains); blank q lists everything. */
    @Transactional(readOnly = true)
    public Page<SettingResponse> search(String q, Pageable pageable) {
        return settingRepository.findAll(TextSearch.<Setting>contains(q, "key", "description"), pageable).map(settingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<SettingResponse> findByScope(String scope) {
        return settingRepository.findByScope(scope).stream()
                .map(settingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SettingResponse> findByScopeAndOrganization(String scope, UUID organizationId) {
        return settingRepository.findByScopeAndOrganizationId(scope, organizationId).stream()
                .map(settingMapper::toResponse)
                .toList();
    }

    @Audited(action = "UPDATE", description = "Update a setting")
    public SettingResponse update(UUID id, UpdateSettingRequest request) {
        Setting entity = getEntityOrThrow(id);
        VersionCheck.check(Setting.class, id, entity.getVersion(), request.version());
        if (!entity.isEditable()) {
            throw new BusinessRuleException(ErrorCode.SETTING_NOT_EDITABLE,
                    "Setting is marked as non-editable and cannot be updated: " + entity.getKey());
        }
        settingMapper.updateEntityFromRequest(request, entity);
        Setting saved = settingRepository.save(entity);
        return settingMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Delete a setting")
    public void delete(UUID id) {
        Setting entity = getEntityOrThrow(id);
        if (!entity.isEditable()) {
            throw new BusinessRuleException(ErrorCode.SETTING_NOT_EDITABLE,
                    "Setting is marked as non-editable and cannot be deleted: " + entity.getKey());
        }
        settingRepository.delete(entity);
    }

    private Setting getEntityOrThrow(UUID id) {
        return settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting", id));
    }
}
