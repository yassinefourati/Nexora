import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import {
  MENUS_BASE_PATH, MENUS_KEY, MENU_ITEMS_BASE_PATH, MENU_ITEMS_KEY,
  type CreateMenuRequest, type UpdateMenuRequest, type Menu,
  type CreateMenuItemRequest, type UpdateMenuItemRequest, type MenuItem,
} from '../api/menusApi';

function menusResource() {
  return useCrudResource<Menu, CreateMenuRequest, UpdateMenuRequest>(MENUS_BASE_PATH, MENUS_KEY);
}
function menuItemsResource() {
  return useCrudResource<MenuItem, CreateMenuItemRequest, UpdateMenuItemRequest>(MENU_ITEMS_BASE_PATH, MENU_ITEMS_KEY);
}

export function useMenus(params: ListParams = {}) {
  return menusResource().useList(params);
}
export function useCreateMenu() {
  return menusResource().useCreate();
}
export function useUpdateMenu() {
  return menusResource().useUpdate();
}
export function useDeleteMenu() {
  return menusResource().useRemove();
}

export function useMenuItems(params: ListParams = {}) {
  return menuItemsResource().useList(params);
}
export function useCreateMenuItem() {
  return menuItemsResource().useCreate();
}
export function useUpdateMenuItem() {
  return menuItemsResource().useUpdate();
}
export function useDeleteMenuItem() {
  return menuItemsResource().useRemove();
}
