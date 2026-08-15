import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { ROLES_BASE_PATH, ROLES_KEY, type CreateRoleRequest, type UpdateRoleRequest, type Role } from '../api/rolesApi';

function resource() {
  return useCrudResource<Role, CreateRoleRequest, UpdateRoleRequest>(ROLES_BASE_PATH, ROLES_KEY);
}

export function useRoles(params: ListParams = {}) {
  return resource().useList(params);
}

export function useRole(id: string | undefined) {
  return resource().useOne(id);
}

export function useCreateRole() {
  return resource().useCreate();
}

export function useUpdateRole() {
  return resource().useUpdate();
}

export function useDeleteRole() {
  return resource().useRemove();
}
