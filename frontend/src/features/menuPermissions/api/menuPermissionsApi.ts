import type { components } from '@/shared/types/api.generated';

export type MenuPermission = components['schemas']['MenuPermissionResponse'];
export type CreateMenuPermissionRequest = components['schemas']['CreateMenuPermissionRequest'];

export const MENU_PERMISSIONS_BASE_PATH = '/menu-permissions';
export const MENU_PERMISSIONS_KEY = 'menu-permissions';
