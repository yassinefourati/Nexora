import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { ROLE_MENUS_BASE_PATH, ROLE_MENUS_KEY, type CreateRoleMenuRequest, type UpdateRoleMenuRequest, type RoleMenu } from '../api/roleMenusApi';

function useResource() {
  return useCrudResource<RoleMenu, CreateRoleMenuRequest, UpdateRoleMenuRequest>(ROLE_MENUS_BASE_PATH, ROLE_MENUS_KEY);
}

export function useRoleMenus(params: ListParams = {}) {
  return useResource().useList(params);
}
export function useCreateRoleMenu() {
  return useResource().useCreate();
}
export function useUpdateRoleMenu() {
  return useResource().useUpdate();
}
export function useDeleteRoleMenu() {
  return useResource().useRemove();
}
