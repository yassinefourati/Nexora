import type { MenuItem } from './config/menuConfig';

/** True when the item, or any descendant, matches the current pathname. */
export function isPathActive(item: MenuItem, pathname: string): boolean {
  if (item.path === pathname) return true;
  return item.children?.some((c) => isPathActive(c, pathname)) ?? false;
}
