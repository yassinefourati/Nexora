import type { components } from '@/shared/types/api.generated';

export type Menu = components['schemas']['MenuResponse'];
export type CreateMenuRequest = components['schemas']['CreateMenuRequest'];
export type UpdateMenuRequest = components['schemas']['UpdateMenuRequest'];

export type MenuItem = components['schemas']['MenuItemResponse'];
export type CreateMenuItemRequest = components['schemas']['CreateMenuItemRequest'];
export type UpdateMenuItemRequest = components['schemas']['UpdateMenuItemRequest'];

export const MENUS_BASE_PATH = '/menus';
export const MENUS_KEY = 'menus';
export const MENU_ITEMS_BASE_PATH = '/menu-items';
export const MENU_ITEMS_KEY = 'menu-items';
