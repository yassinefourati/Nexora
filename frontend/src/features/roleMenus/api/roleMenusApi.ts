import type { components } from '@/shared/types/api.generated';

export type RoleMenu = components['schemas']['RoleMenuResponse'];
export type CreateRoleMenuRequest = components['schemas']['CreateRoleMenuRequest'];
export type UpdateRoleMenuRequest = components['schemas']['UpdateRoleMenuRequest'];

export const ROLE_MENUS_BASE_PATH = '/role-menus';
export const ROLE_MENUS_KEY = 'role-menus';
