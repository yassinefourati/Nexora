package com.fourati.service;

import com.fourati.domain.Permission;
import com.fourati.dto.request.CreatePermissionRequest;
import com.fourati.mapper.PermissionMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers PermissionService's uniqueness constraint on (resource, action) —
 * the pair every backend @PreAuthorize check and the frontend's
 * usePermission()/<Can> ultimately key off of.
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    void create_duplicateResourceAction_throwsConflict_neverSaves() {
        CreatePermissionRequest request = new CreatePermissionRequest("user", "read", "desc");
        when(permissionRepository.existsByResourceAndAction("user", "read")).thenReturn(true);

        assertThatThrownBy(() -> permissionService.create(request)).isInstanceOf(ConflictException.class);

        verify(permissionRepository, never()).save(any());
    }

    @Test
    void create_novelResourceAction_savesSuccessfully() {
        CreatePermissionRequest request = new CreatePermissionRequest("user", "read", "desc");
        Permission mapped = new Permission();
        when(permissionRepository.existsByResourceAndAction("user", "read")).thenReturn(false);
        when(permissionMapper.toEntity(request)).thenReturn(mapped);
        when(permissionRepository.save(mapped)).thenReturn(mapped);

        permissionService.create(request);

        verify(permissionRepository).save(mapped);
    }

    @Test
    void delete_notFound_throwsResourceNotFound_neverCallsRepositoryDelete() {
        UUID id = UUID.randomUUID();
        when(permissionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.delete(id)).isInstanceOf(ResourceNotFoundException.class);

        verify(permissionRepository, never()).delete(any());
    }

    @Test
    void delete_found_hardDeletesTheEntity() {
        UUID id = UUID.randomUUID();
        Permission entity = new Permission();
        when(permissionRepository.findById(id)).thenReturn(Optional.of(entity));

        permissionService.delete(id);

        verify(permissionRepository).delete(entity);
    }
}
