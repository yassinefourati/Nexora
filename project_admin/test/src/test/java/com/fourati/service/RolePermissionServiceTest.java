package com.fourati.service;

import com.fourati.domain.Permission;
import com.fourati.domain.Role;
import com.fourati.domain.RolePermission;
import com.fourati.dto.request.CreateRolePermissionRequest;
import com.fourati.dto.response.PermissionResponse;
import com.fourati.dto.response.RolePermissionResponse;
import com.fourati.mapper.PermissionMapper;
import com.fourati.mapper.RolePermissionMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.PermissionRepository;
import com.fourati.repository.RolePermissionRepository;
import com.fourati.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Covers RolePermissionService.findEffectivePermissionsByRoleNames -- the query
 * MeController./me/permissions ultimately depends on to resolve every
 * frontend permission check (usePermission/<Can>) -- plus the create() conflict
 * check, since a duplicate grant here would silently double up a role's
 * effective permissions.
 */
@ExtendWith(MockitoExtension.class)
class RolePermissionServiceTest {

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private RolePermissionService rolePermissionService;

    @Test
    void findEffectivePermissionsByRoleNames_emptyRoleNames_returnsEmptyWithoutQuerying() {
        List<PermissionResponse> result = rolePermissionService.findEffectivePermissionsByRoleNames(List.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(rolePermissionRepository);
    }

    @Test
    void findEffectivePermissionsByRoleNames_resolvesDistinctPermissionsForGivenRoles() {
        Permission readUsers = permission("user", "read");
        Permission writeUsers = permission("user", "write");
        PermissionResponse readUsersResponse = permissionResponse(readUsers);
        PermissionResponse writeUsersResponse = permissionResponse(writeUsers);

        when(rolePermissionRepository.findDistinctPermissionsByRoleNameIn(anyCollection()))
                .thenReturn(List.of(readUsers, writeUsers));
        when(permissionMapper.toResponse(readUsers)).thenReturn(readUsersResponse);
        when(permissionMapper.toResponse(writeUsers)).thenReturn(writeUsersResponse);

        List<PermissionResponse> result = rolePermissionService.findEffectivePermissionsByRoleNames(List.of("admin"));

        assertThat(result).containsExactlyInAnyOrder(readUsersResponse, writeUsersResponse);
        verify(rolePermissionRepository).findDistinctPermissionsByRoleNameIn(List.of("admin"));
    }

    @Test
    void findEffectivePermissionsByRoleNames_unknownRoleName_returnsEmptyNotError() {
        when(rolePermissionRepository.findDistinctPermissionsByRoleNameIn(anyCollection()))
                .thenReturn(List.of());

        List<PermissionResponse> result = rolePermissionService
                .findEffectivePermissionsByRoleNames(List.of("does-not-exist-as-a-backend-role"));

        assertThat(result).isEmpty();
    }

    @Test
    void create_duplicateGrant_throwsConflictBeforeTouchingRoleOrPermissionLookup() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        CreateRolePermissionRequest request = new CreateRolePermissionRequest(roleId, permissionId, null);

        when(rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)).thenReturn(true);

        assertThatThrownBy(() -> rolePermissionService.create(request))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(roleRepository);
        verifyNoInteractions(permissionRepository);
    }

    @Test
    void create_roleNotFound_throwsResourceNotFound() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        CreateRolePermissionRequest request = new CreateRolePermissionRequest(roleId, permissionId, null);

        when(rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)).thenReturn(false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolePermissionService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByRoleId_mapsEveryGrantThroughResponseMapper() {
        UUID roleId = UUID.randomUUID();
        RolePermission grant = new RolePermission();
        RolePermissionResponse response = new RolePermissionResponse(UUID.randomUUID(), roleId, UUID.randomUUID(), null, null, null);

        when(rolePermissionRepository.findByRoleId(roleId)).thenReturn(List.of(grant));
        when(rolePermissionMapper.toResponse(grant)).thenReturn(response);

        List<RolePermissionResponse> result = rolePermissionService.findByRoleId(roleId);

        assertThat(result).containsExactly(response);
    }

    private Permission permission(String resource, String action) {
        Permission permission = new Permission();
        permission.setResource(resource);
        permission.setAction(action);
        return permission;
    }

    private PermissionResponse permissionResponse(Permission permission) {
        return new PermissionResponse(UUID.randomUUID(), permission.getResource(), permission.getAction(),
                permission.getResource() + "." + permission.getAction(), null, null, null);
    }
}
