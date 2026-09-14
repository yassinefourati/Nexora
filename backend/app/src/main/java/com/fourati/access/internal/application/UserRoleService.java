package com.fourati.access.internal.application;


import com.fourati.access.internal.domain.UserRole;
import com.fourati.access.UserRoleResponse;
import com.fourati.access.internal.persistence.UserRoleRepository;
import com.fourati.platform.audit.Audited;
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
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRoleMapper userRoleMapper;

    @Transactional(readOnly = true)
    public UserRoleResponse findById(UUID id) {
        return userRoleMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<UserRoleResponse> findAll(Pageable pageable) {
        return userRoleRepository.findAll(pageable).map(userRoleMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponse> findByUserId(UUID userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(userRoleMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<UserRoleResponse> findByRoleId(UUID roleId, Pageable pageable) {
        return userRoleRepository.findByRoleId(roleId, pageable).map(userRoleMapper::toResponse);
    }

    @Audited(action = "DELETE", description = "Remove a role assignment from a user")
    public void delete(UUID id) {
        UserRole entity = getEntityOrThrow(id);
        userRoleRepository.delete(entity);
    }

    private UserRole getEntityOrThrow(UUID id) {
        return userRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserRole", id));
    }
}
