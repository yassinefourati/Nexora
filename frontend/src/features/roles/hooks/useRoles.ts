import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { ROLES_BASE_PATH, ROLES_KEY, type CreateRoleRequest, type UpdateRoleRequest, type Role } from '../api/rolesApi';

function useResource() {
  return useCrudResource<Role, CreateRoleRequest, UpdateRoleRequest>(ROLES_BASE_PATH, ROLES_KEY);
}

export function useRoles(params: ListParams = {}) {
  return useResource().useList(params);
}

export function useRole(id: string | undefined) {
  return useResource().useOne(id);
}

export function useCreateRole() {
  return useResource().useCreate();
}

export function useUpdateRole() {
  return useResource().useUpdate();
}

export function useDeleteRole() {
  return useResource().useRemove();
}
