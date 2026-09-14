import { useMemo, useEffect } from 'react';
import Fuse from 'fuse.js';
import { useNavigate, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ROUTES, GUIDE_ROUTE } from '@/core/router/routes';
import { useUIStore } from '@/shared/stores/useUIStore';
import { useUsers } from '@/features/users/hooks/useUsers';

export interface SearchItem {
  id: string; label: string; description?: string;
  category: 'navigation' | 'user' | 'recent'; path?: string; icon?: string;
}

/**
 * Navigation targets offered by the command palette. Every `path` must be a
 * registered route (see `useGlobalSearch.test.ts`), so the palette can never
 * send a user to a 404. Labels are i18n keys resolved at render time.
 */
export const NAV_TARGETS: ReadonlyArray<{ id: string; labelKey: string; path: string; icon: string; devOnly?: boolean }> = [
  { id: 'nav-dashboard',     labelKey: 'menu.dashboard',                  path: ROUTES.HOME,          icon: '🏠' },
  { id: 'nav-users',         labelKey: 'menu.users',                      path: ROUTES.USERS,         icon: '👥' },
  { id: 'nav-roles',         labelKey: 'menu.roles',                      path: ROUTES.USERS_ROLES,   icon: '🛡️' },
  { id: 'nav-permissions',   labelKey: 'menu.permissions',                path: ROUTES.USERS_PERMISSIONS, icon: '🔑' },
  { id: 'nav-organizations', labelKey: 'resources.organizations.title',   path: ROUTES.ORGANIZATIONS, icon: '🏢' },
  { id: 'nav-departments',   labelKey: 'resources.departments.title',     path: ROUTES.DEPARTMENTS,   icon: '🏬' },
  { id: 'nav-teams',         labelKey: 'resources.teams.title',           path: ROUTES.TEAMS,         icon: '👫' },
  { id: 'nav-settings',      labelKey: 'menu.settings',                   path: ROUTES.SETTINGS,      icon: '⚙️' },
  { id: 'nav-feature-flags', labelKey: 'resources.featureFlags.title',    path: ROUTES.FEATURE_FLAGS, icon: '🚩' },
  { id: 'nav-menus',         labelKey: 'resources.menus.title',           path: ROUTES.MENUS,         icon: '📂' },
  { id: 'nav-profile',       labelKey: 'profile.title',                   path: ROUTES.PROFILE,       icon: '👤' },
  { id: 'nav-audit',         labelKey: 'audit.title',                     path: ROUTES.AUDIT,         icon: '📋' },
  { id: 'nav-sessions',      labelKey: 'resources.sessions.title',        path: ROUTES.SESSIONS,      icon: '🔐' },
  { id: 'nav-api-keys',      labelKey: 'resources.apiKeys.title',         path: ROUTES.API_KEYS,      icon: '🗝️' },
  { id: 'nav-notifications', labelKey: 'notifications.title',             path: ROUTES.NOTIFICATIONS, icon: '🔔' },
  { id: 'nav-system',        labelKey: 'system.title',                    path: ROUTES.SYSTEM,        icon: '🖥️' },
  { id: 'nav-guide',         labelKey: 'menu.developerGuide',             path: GUIDE_ROUTE,          icon: '📖', devOnly: true },
  { id: 'nav-docs',          labelKey: 'menu.devDocs',                    path: ROUTES.DOCS,          icon: '📚', devOnly: true },
  { id: 'nav-uidocs',        labelKey: 'menu.uiLibrary',                  path: ROUTES.UI_DOCS,       icon: '🎨', devOnly: true },
];

export function useGlobalSearch(query: string) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { t } = useTranslation();
  const { recentPages, addRecentPage } = useUIStore();

  const navItems = useMemo<SearchItem[]>(() =>
    NAV_TARGETS
      .filter((n) => !n.devOnly || import.meta.env.DEV)
      .map((n) => ({ id: n.id, label: t(n.labelKey), category: 'navigation', path: n.path, icon: n.icon })),
    [t]);

  // Pre-fetch first page of users to seed search index
  const { data: usersData } = useUsers({ page: 0, size: 50 });

  const allItems = useMemo<SearchItem[]>(() => {
    const userItems: SearchItem[] = (usersData?.items ?? []).map((u) => ({
      id: `user-${u.id}`,
      label: u.username ?? '',
      description: u.email,
      category: 'user',
      path: `/users/${u.id}`,
      icon: '👤',
    }));
    return [...navItems, ...userItems];
  }, [usersData, navItems]);

  const fuse = useMemo(() => new Fuse(allItems, { keys: ['label', 'description'], threshold: 0.4 }), [allItems]);

  const results = useMemo(() => {
    if (query.length < 1) {
      const recent: SearchItem[] = recentPages.map((p) => ({
        id: `recent-${p.path}`, label: p.label, category: 'recent',
        path: p.path, icon: '🕐',
      }));
      return [...recent, ...navItems].slice(0, 8);
    }
    return fuse.search(query).map((r) => r.item);
  }, [query, fuse, recentPages, navItems]);

  const navigate_to = (item: SearchItem) => {
    if (item.path) { navigate(item.path); addRecentPage(item.path, item.label); }
  };

  useEffect(() => {
    const found = allItems.find((i) => i.path === pathname && i.category === 'navigation');
    if (found?.path) addRecentPage(found.path, found.label);
  }, [pathname, addRecentPage, allItems]);

  return { results, navigate_to };
}
