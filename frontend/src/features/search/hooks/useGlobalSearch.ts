import { useMemo, useEffect } from 'react';
import Fuse from 'fuse.js';
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES, GUIDE_ROUTE } from '@/core/router/routes';
import { useUIStore } from '@/shared/stores/useUIStore';
import { useUsers } from '@/features/users/hooks/useUsers';

export interface SearchItem {
  id: string; label: string; description?: string;
  category: 'navigation' | 'user' | 'recent'; path?: string; icon?: string;
}

const NAV_ITEMS: SearchItem[] = [
  { id:'nav-dashboard',     label:'Dashboard',             category:'navigation', path:ROUTES.HOME,                   icon:'🏠' },
  { id:'nav-users',         label:'Users',                 category:'navigation', path:ROUTES.USERS,                  icon:'👥' },
  { id:'nav-roles',         label:'Roles & Permissions',   category:'navigation', path:ROUTES.USERS_ROLES,            icon:'🛡️' },
  { id:'nav-organizations', label:'Organizations',         category:'navigation', path:ROUTES.ORGANIZATIONS,          icon:'🏢' },
  { id:'nav-settings',      label:'Settings',              category:'navigation', path:ROUTES.SETTINGS,               icon:'⚙️' },
  { id:'nav-profile',       label:'My Profile',            category:'navigation', path:ROUTES.PROFILE,                icon:'👤' },
  { id:'nav-audit',         label:'Audit Log',             category:'navigation', path:ROUTES.AUDIT,                  icon:'📋' },
  { id:'nav-notifications', label:'Notifications',         category:'navigation', path:ROUTES.NOTIFICATIONS,          icon:'🔔' },
  { id:'nav-reports',       label:'Reports',               category:'navigation', path:'/reports',                    icon:'📑' },
  { id:'nav-system',        label:'System Health',         category:'navigation', path:'/system',                     icon:'🖥️' },
  { id:'nav-invite',        label:'Invite User',           category:'navigation', path:'/invite',                     icon:'✉️' },
  { id:'nav-guide',         label:'Developer Guide',       category:'navigation', path:GUIDE_ROUTE,                   icon:'📖' },
  { id:'nav-docs',          label:'Dev Docs',              category:'navigation', path:'/docs',                       icon:'📚' },
  { id:'nav-uidocs',        label:'UI Component Library',  category:'navigation', path:'/ui-docs',                    icon:'🎨' },
];

export function useGlobalSearch(query: string) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { recentPages, addRecentPage } = useUIStore();

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
    return [...NAV_ITEMS, ...userItems];
  }, [usersData]);

  const fuse = useMemo(() => new Fuse(allItems, { keys: ['label', 'description'], threshold: 0.4 }), [allItems]);

  const results = useMemo(() => {
    if (query.length < 1) {
      const recent: SearchItem[] = recentPages.map((p) => ({
        id: `recent-${p.path}`, label: p.label, category: 'recent',
        path: p.path, icon: '🕐',
      }));
      return [...recent, ...NAV_ITEMS].slice(0, 8);
    }
    return fuse.search(query).map((r) => r.item);
  }, [query, fuse, recentPages]);

  const navigate_to = (item: SearchItem) => {
    if (item.path) { navigate(item.path); addRecentPage(item.path, item.label); }
  };

  useEffect(() => {
    const found = allItems.find((i) => i.path === pathname && i.category === 'navigation');
    if (found?.path) addRecentPage(found.path, found.label);
  }, [pathname, addRecentPage, allItems]);

  return { results, navigate_to };
}
