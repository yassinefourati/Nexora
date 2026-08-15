package com.fourati.service;

import com.fourati.domain.Role;
import com.fourati.domain.User;
import com.fourati.domain.UserRole;
import com.fourati.dto.request.CreateUserRoleRequest;
import com.fourati.dto.response.UserRoleResponse;
import com.fourati.mapper.UserRoleMapper;
import com.fourati.repository.RoleRepository;
import com.fourati.repository.UserRepository;
import com.fourati.repository.UserRoleRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMapper userRoleMapper;

    @Audited(action = "CREATE", description = "Assign a role to a user")
    public UserRoleResponse create(CreateUserRoleRequest request) {
        if (userRoleRepository.existsByUserIdAndRoleIdAndOrganizationId(
                request.userId(), request.roleId(), request.organizationId())) {
            throw new ConflictException("User already has this role assigned for the given organization");
        }
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", request.roleId()));
        UserRole entity = userRoleMapper.toEntity(request);
        entity.setUser(user);
        entity.setRole(role);
        entity.setAssignedAt(Instant.now());
        UserRole saved = userRoleRepository.save(entity);
        return userRoleMapper.toResponse(saved);
    }

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
