import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import {
  MENUS_BASE_PATH, MENUS_KEY, MENU_ITEMS_BASE_PATH, MENU_ITEMS_KEY,
  type CreateMenuRequest, type UpdateMenuRequest, type Menu,
  type CreateMenuItemRequest, type UpdateMenuItemRequest, type MenuItem,
} from '../api/menusApi';

function useMenusResource() {
  return useCrudResource<Menu, CreateMenuRequest, UpdateMenuRequest>(MENUS_BASE_PATH, MENUS_KEY);
}
function useMenuItemsResource() {
  return useCrudResource<MenuItem, CreateMenuItemRequest, UpdateMenuItemRequest>(MENU_ITEMS_BASE_PATH, MENU_ITEMS_KEY);
}

export function useMenus(params: ListParams = {}) {
  return useMenusResource().useList(params);
}
export function useCreateMenu() {
  return useMenusResource().useCreate();
}
export function useUpdateMenu() {
  return useMenusResource().useUpdate();
}
export function useDeleteMenu() {
  return useMenusResource().useRemove();
}

export function useMenuItems(params: ListParams = {}) {
  return useMenuItemsResource().useList(params);
}
export function useCreateMenuItem() {
  return useMenuItemsResource().useCreate();
}
export function useUpdateMenuItem() {
  return useMenuItemsResource().useUpdate();
}
export function useDeleteMenuItem() {
  return useMenuItemsResource().useRemove();
}
