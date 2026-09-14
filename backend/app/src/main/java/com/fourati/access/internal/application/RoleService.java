package com.fourati.access.internal.application;

import com.fourati.shared.persistence.TextSearch;

import com.fourati.shared.persistence.VersionCheck;

import com.fourati.access.internal.domain.Role;
import com.fourati.access.CreateRoleRequest;
import com.fourati.access.UpdateRoleRequest;
import com.fourati.access.RoleResponse;
import com.fourati.platform.error.BusinessRuleException;
import com.fourati.platform.error.ErrorCode;
import com.fourati.access.internal.persistence.RoleRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Audited(action = "CREATE", description = "Create a new role")
    public RoleResponse create(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.name())) {
            throw new ConflictException("Role already exists with name: " + request.name());
        }
        Role entity = roleMapper.toEntity(request);
        Role saved = roleRepository.save(entity);
        return roleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RoleResponse findById(UUID id) {
        return roleMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable).map(roleMapper::toResponse);
    }

    /** Server-side search over name, description (case-insensitive contains); blank q lists everything. */
    @Transactional(readOnly = true)
    public Page<RoleResponse> search(String q, Pageable pageable) {
        return roleRepository.findAll(TextSearch.<Role>contains(q, "name", "description"), pageable).map(roleMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Update a role")
    public RoleResponse update(UUID id, UpdateRoleRequest request) {
        Role entity = getEntityOrThrow(id);
        VersionCheck.check(Role.class, id, entity.getVersion(), request.version());
        if (!entity.getName().equals(request.name()) && roleRepository.existsByName(request.name())) {
            throw new ConflictException("Role already exists with name: " + request.name());
        }
        roleMapper.updateEntityFromRequest(request, entity);
        Role saved = roleRepository.save(entity);
        return roleMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a role")
    public void delete(UUID id) {
        Role entity = getEntityOrThrow(id);
        if (entity.isSystem()) {
            throw new BusinessRuleException(ErrorCode.BUSINESS_RULE_VIOLATION, "System roles cannot be deleted");
        }
        entity.setDeletedAt(Instant.now());
        roleRepository.save(entity);
    }

    private Role getEntityOrThrow(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }
}
