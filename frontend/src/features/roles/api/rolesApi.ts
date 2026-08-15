import type { components } from '@/shared/types/api.generated';

export type Role = components['schemas']['RoleResponse'];
export type CreateRoleRequest = components['schemas']['CreateRoleRequest'];
export type UpdateRoleRequest = components['schemas']['UpdateRoleRequest'];

export const ROLES_BASE_PATH = '/roles';
export const ROLES_KEY = 'roles';
