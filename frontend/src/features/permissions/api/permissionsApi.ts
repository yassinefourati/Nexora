import type { components } from '@/shared/types/api.generated';

export type Permission = components['schemas']['PermissionResponse'];
export type CreatePermissionRequest = components['schemas']['CreatePermissionRequest'];
export type UpdatePermissionRequest = components['schemas']['UpdatePermissionRequest'];
export type RolePermission = components['schemas']['RolePermissionResponse'];
export type CreateRolePermissionRequest = components['schemas']['CreateRolePermissionRequest'];

export const PERMISSIONS_BASE_PATH = '/permissions';
export const PERMISSIONS_KEY = 'permissions';
export const ROLE_PERMISSIONS_BASE_PATH = '/role-permissions';
export const ROLE_PERMISSIONS_KEY = 'role-permissions';

/** Valid action values per CreatePermissionRequest's backend-enforced pattern. */
export const PERMISSION_ACTIONS = ['read', 'write', 'edit', 'delete', 'execute', 'approve'] as const;
