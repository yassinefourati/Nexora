package com.fourati.service;

import com.fourati.domain.Role;
import com.fourati.dto.request.CreateRoleRequest;
import com.fourati.dto.request.UpdateRoleRequest;
import com.fourati.error.BusinessRuleException;
import com.fourati.mapper.RoleMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.RoleRepository;
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
 * Covers RoleService's business rules — most importantly that system roles
 * (admin/manager/user, seeded via V9) cannot be deleted, since that's the one
 * piece of real domain logic in this service beyond plain CRUD.
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleService roleService;

    @Test
    void create_duplicateName_throwsConflict() {
        CreateRoleRequest request = new CreateRoleRequest("admin", "desc", false);
        when(roleRepository.existsByName("admin")).thenReturn(true);

        assertThatThrownBy(() -> roleService.create(request)).isInstanceOf(ConflictException.class);

        verify(roleRepository, never()).save(any());
    }

    @Test
    void delete_systemRole_throwsBusinessRuleException_neverSaves() {
        UUID id = UUID.randomUUID();
        Role systemRole = new Role();
        systemRole.setName("admin");
        systemRole.setSystem(true);
        when(roleRepository.findById(id)).thenReturn(Optional.of(systemRole));

        assertThatThrownBy(() -> roleService.delete(id)).isInstanceOf(BusinessRuleException.class);

        verify(roleRepository, never()).save(any());
    }

    @Test
    void delete_nonSystemRole_softDeletesSuccessfully() {
        UUID id = UUID.randomUUID();
        Role customRole = new Role();
        customRole.setName("custom-role");
        customRole.setSystem(false);
        when(roleRepository.findById(id)).thenReturn(Optional.of(customRole));

        roleService.delete(id);

        verify(roleRepository).save(customRole);
    }

    @Test
    void delete_notFound_throwsResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(roleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.delete(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_renamingToAnotherExistingRolesName_throwsConflict() {
        UUID id = UUID.randomUUID();
        Role existing = new Role();
        existing.setName("original-name");
        existing.setSystem(false);
        UpdateRoleRequest request = new UpdateRoleRequest("taken-name", "desc", false);

        when(roleRepository.findById(id)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByName("taken-name")).thenReturn(true);

        assertThatThrownBy(() -> roleService.update(id, request)).isInstanceOf(ConflictException.class);
    }

    @Test
    void update_keepingSameName_doesNotTreatItAsAConflict() {
        UUID id = UUID.randomUUID();
        Role existing = new Role();
        existing.setName("unchanged-name");
        existing.setSystem(false);
        UpdateRoleRequest request = new UpdateRoleRequest("unchanged-name", "new description", false);

        when(roleRepository.findById(id)).thenReturn(Optional.of(existing));
        when(roleRepository.save(existing)).thenReturn(existing);

        roleService.update(id, request);

        verify(roleRepository, never()).existsByName(any());
        verify(roleRepository).save(existing);
    }
}
