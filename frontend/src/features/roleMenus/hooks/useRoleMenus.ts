import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { ROLE_MENUS_BASE_PATH, ROLE_MENUS_KEY, type CreateRoleMenuRequest, type UpdateRoleMenuRequest, type RoleMenu } from '../api/roleMenusApi';

function resource() {
  return useCrudResource<RoleMenu, CreateRoleMenuRequest, UpdateRoleMenuRequest>(ROLE_MENUS_BASE_PATH, ROLE_MENUS_KEY);
}

export function useRoleMenus(params: ListParams = {}) {
  return resource().useList(params);
}
export function useCreateRoleMenu() {
  return resource().useCreate();
}
export function useUpdateRoleMenu() {
  return resource().useUpdate();
}
export function useDeleteRoleMenu() {
  return resource().useRemove();
}
