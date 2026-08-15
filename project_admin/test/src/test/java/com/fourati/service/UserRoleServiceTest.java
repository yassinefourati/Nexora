package com.fourati.service;

import com.fourati.domain.Role;
import com.fourati.domain.User;
import com.fourati.domain.UserRole;
import com.fourati.dto.request.CreateUserRoleRequest;
import com.fourati.mapper.UserRoleMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.RoleRepository;
import com.fourati.repository.UserRepository;
import com.fourati.repository.UserRoleRepository;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Covers UserRoleService.create() — this is the write path that ultimately
 * feeds every downstream permission check (usePermission/<Can> and
 * /me/permissions resolve through the roles a user has here), so a bad grant
 * here is a real RBAC correctness issue, not just a CRUD nitpick.
 */
@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleMapper userRoleMapper;

    @InjectMocks
    private UserRoleService userRoleService;

    @Test
    void create_duplicateAssignment_throwsConflict_neverLooksUpUserOrRole() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        CreateUserRoleRequest request = new CreateUserRoleRequest(userId, roleId, null, null);

        when(userRoleRepository.existsByUserIdAndRoleIdAndOrganizationId(userId, roleId, null)).thenReturn(true);

        assertThatThrownBy(() -> userRoleService.create(request)).isInstanceOf(ConflictException.class);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleRepository);
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void create_userDoesNotExist_throwsResourceNotFound_neverLooksUpRole() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        CreateUserRoleRequest request = new CreateUserRoleRequest(userId, roleId, null, null);

        when(userRoleRepository.existsByUserIdAndRoleIdAndOrganizationId(userId, roleId, null)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userRoleService.create(request)).isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(roleRepository);
    }

    @Test
    void create_roleDoesNotExist_throwsResourceNotFound() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        CreateUserRoleRequest request = new CreateUserRoleRequest(userId, roleId, null, null);

        when(userRoleRepository.existsByUserIdAndRoleIdAndOrganizationId(userId, roleId, null)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userRoleService.create(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_validAssignment_setsUserAndRoleOnTheEntityBeforeSaving() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        CreateUserRoleRequest request = new CreateUserRoleRequest(userId, roleId, null, null);
        User user = new User();
        Role role = new Role();
        UserRole mapped = new UserRole();

        when(userRoleRepository.existsByUserIdAndRoleIdAndOrganizationId(userId, roleId, null)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRoleMapper.toEntity(request)).thenReturn(mapped);
        when(userRoleRepository.save(mapped)).thenReturn(mapped);

        userRoleService.create(request);

        org.assertj.core.api.Assertions.assertThat(mapped.getUser()).isSameAs(user);
        org.assertj.core.api.Assertions.assertThat(mapped.getRole()).isSameAs(role);
        org.assertj.core.api.Assertions.assertThat(mapped.getAssignedAt()).isNotNull();
        verify(userRoleRepository).save(mapped);
    }
}
