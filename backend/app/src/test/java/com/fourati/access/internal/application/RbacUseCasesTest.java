package com.fourati.access.internal.application;

import com.fourati.identity.UserStatus;

import com.fourati.access.AccessChanged;
import com.fourati.access.AssignRoleRequest;
import com.fourati.access.GrantPermissionsRequest;
import com.fourati.access.RevokeAccessResponse;
import com.fourati.access.RolePermissionResponse;
import com.fourati.access.UserRoleResponse;
import com.fourati.access.internal.domain.Permission;
import com.fourati.access.internal.domain.Role;
import com.fourati.access.internal.domain.RolePermission;
import com.fourati.access.internal.domain.UserRole;
import com.fourati.access.internal.persistence.PermissionRepository;
import com.fourati.access.internal.persistence.RolePermissionRepository;
import com.fourati.access.internal.persistence.RoleRepository;
import com.fourati.access.internal.persistence.UserRoleRepository;
import com.fourati.identity.IdentityFacade;
import com.fourati.identity.RevokedCredentials;
import com.fourati.identity.UserSummary;
import com.fourati.platform.error.BusinessRuleException;
import com.fourati.platform.error.ResourceNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Business rules of the access-management use cases, independent of HTTP and
 * the database. The identity module is a facade here: the use cases know a
 * user only as an id plus the summary the facade returns.
 */
class RbacUseCasesTest {

    static UserSummary user(String status) {
        UUID id = UUID.randomUUID();
        return new UserSummary(id, "u-" + id, UserStatus.fromValue(status));
    }

    static Role role(String name) {
        Role r = new Role();
        r.setId(UUID.randomUUID());
        r.setName(name);
        return r;
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class AssignRole {
        @Mock IdentityFacade identity;
        @Mock RoleRepository roleRepository;
        @Mock UserRoleRepository userRoleRepository;
        @Mock UserRoleMapper userRoleMapper;
        @Mock ApplicationEventPublisher eventPublisher;
        @InjectMocks AssignRoleToUser useCase;

        @Test
        void grantsRole_whenUserActiveAndNotAlreadyGranted() {
            UserSummary u = user("active");
            Role r = role("manager");
            when(identity.find(u.id())).thenReturn(Optional.of(u));
            when(roleRepository.findById(r.getId())).thenReturn(Optional.of(r));
            when(userRoleRepository.existsByUserIdAndRoleIdAndOrganizationId(u.id(), r.getId(), null)).thenReturn(false);
            when(userRoleRepository.save(any(UserRole.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userRoleMapper.toResponse(any(UserRole.class))).thenReturn(new UserRoleResponse(null, null, null, null, null, null, null, null, null));

            useCase.execute(u.id(), new AssignRoleRequest(r.getId(), null, null));

            ArgumentCaptor<UserRole> captor = ArgumentCaptor.forClass(UserRole.class);
            verify(userRoleRepository).save(captor.capture());
            assertThat(captor.getValue().getUserId()).isEqualTo(u.id());
            assertThat(captor.getValue().getRole()).isSameAs(r);
            assertThat(captor.getValue().getAssignedAt()).isNotNull();
            verify(eventPublisher).publishEvent(any(AccessChanged.class));
        }

        @Test
        void refuses_suspendedUser() {
            UserSummary u = user("suspended");
            when(identity.find(u.id())).thenReturn(Optional.of(u));

            assertThatThrownBy(() -> useCase.execute(u.id(), new AssignRoleRequest(UUID.randomUUID(), null, null)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("suspended");
            verify(userRoleRepository, never()).save(any());
        }

        @Test
        void refuses_unknownUser() {
            UUID id = UUID.randomUUID();
            when(identity.find(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(id, new AssignRoleRequest(UUID.randomUUID(), null, null)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void refuses_pastExpiry() {
            UserSummary u = user("active");
            Role r = role("manager");
            when(identity.find(u.id())).thenReturn(Optional.of(u));
            when(roleRepository.findById(r.getId())).thenReturn(Optional.of(r));

            assertThatThrownBy(() -> useCase.execute(u.id(), new AssignRoleRequest(r.getId(), null, Instant.now().minusSeconds(60))))
                    .isInstanceOf(BusinessRuleException.class)
                    .extracting("errorCode").isEqualTo("ROLE_ASSIGNMENT_EXPIRED");
        }

        @Test
        void refuses_duplicateGrant() {
            UserSummary u = user("active");
            Role r = role("manager");
            when(identity.find(u.id())).thenReturn(Optional.of(u));
            when(roleRepository.findById(r.getId())).thenReturn(Optional.of(r));
            when(userRoleRepository.existsByUserIdAndRoleIdAndOrganizationId(u.id(), r.getId(), null)).thenReturn(true);

            assertThatThrownBy(() -> useCase.execute(u.id(), new AssignRoleRequest(r.getId(), null, null)))
                    .isInstanceOf(BusinessRuleException.class)
                    .extracting("errorCode").isEqualTo("ROLE_ALREADY_ASSIGNED");
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class GrantPermissions {
        @Mock RoleRepository roleRepository;
        @Mock PermissionRepository permissionRepository;
        @Mock RolePermissionRepository rolePermissionRepository;
        @Mock RolePermissionMapper rolePermissionMapper;
        @Mock ApplicationEventPublisher eventPublisher;
        @InjectMocks GrantPermissionsToRole useCase;

        @Test
        void skipsAlreadyGranted_andGrantsTheRest_inOneCall() {
            Role r = role("manager");
            Permission p1 = new Permission(); p1.setId(UUID.randomUUID());
            Permission p2 = new Permission(); p2.setId(UUID.randomUUID());
            when(roleRepository.findById(r.getId())).thenReturn(Optional.of(r));
            when(permissionRepository.findAllById(anyList())).thenReturn(List.of(p1, p2));
            when(rolePermissionRepository.existsByRoleIdAndPermissionId(r.getId(), p1.getId())).thenReturn(true);
            when(rolePermissionRepository.existsByRoleIdAndPermissionId(r.getId(), p2.getId())).thenReturn(false);
            when(rolePermissionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(rolePermissionMapper.toResponse(any(RolePermission.class)))
                    .thenReturn(new RolePermissionResponse(null, null, null, null, null, null, null));

            List<RolePermissionResponse> out = useCase.execute(r.getId(), new GrantPermissionsRequest(List.of(p1.getId(), p2.getId(), p2.getId())));

            assertThat(out).hasSize(1);
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<RolePermission>> captor = ArgumentCaptor.forClass(List.class);
            verify(rolePermissionRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(1);
            assertThat(captor.getValue().get(0).getPermission()).isSameAs(p2);
            verify(eventPublisher).publishEvent(any(AccessChanged.class));
        }

        @Test
        void unknownPermission_failsBeforeAnythingIsWritten() {
            Role r = role("manager");
            Permission p1 = new Permission(); p1.setId(UUID.randomUUID());
            UUID missing = UUID.randomUUID();
            when(roleRepository.findById(r.getId())).thenReturn(Optional.of(r));
            when(permissionRepository.findAllById(anyList())).thenReturn(List.of(p1));

            assertThatThrownBy(() -> useCase.execute(r.getId(), new GrantPermissionsRequest(List.of(p1.getId(), missing))))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(rolePermissionRepository, never()).saveAll(any());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class RevokeAccess {
        @Mock IdentityFacade identity;
        @Mock UserRoleRepository userRoleRepository;
        @Mock ApplicationEventPublisher eventPublisher;
        @InjectMocks RevokeUserAccess useCase;

        @Test
        void removesRoles_andReportsWhatIdentityRevoked() {
            UUID id = UUID.randomUUID();
            UserRole ur = new UserRole();
            when(identity.revokeCredentialsAndSuspend(id)).thenReturn(new RevokedCredentials(2, 1, "suspended"));
            when(userRoleRepository.findByUserId(id)).thenReturn(List.of(ur));

            RevokeAccessResponse out = useCase.execute(id);

            assertThat(out.rolesRemoved()).isEqualTo(1);
            assertThat(out.sessionsRevoked()).isEqualTo(2);
            assertThat(out.apiKeysRevoked()).isEqualTo(1);
            assertThat(out.status()).isEqualTo("suspended");
            verify(userRoleRepository).deleteAll(List.of(ur));
            verify(eventPublisher).publishEvent(any(AccessChanged.class));
        }

        @Test
        void unknownUser_is404_andTouchesNoRoles() {
            UUID id = UUID.randomUUID();
            when(identity.revokeCredentialsAndSuspend(id)).thenThrow(new ResourceNotFoundException("User", id));

            assertThatThrownBy(() -> useCase.execute(id)).isInstanceOf(ResourceNotFoundException.class);
            verify(userRoleRepository, never()).deleteAll(any());
        }
    }
}
