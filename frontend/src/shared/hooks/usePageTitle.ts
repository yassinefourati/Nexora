import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { env } from '@/core/config/env';

const TITLE_MAP: Record<string, string> = {
  '/':                              'Dashboard',
  '/users':                         'Users',
  '/users/roles':                   'Roles & Permissions',
  '/users/permissions':             'Permissions',
  '/analytics':                     'Analytics',
  '/analytics/reports/monthly':     'Monthly Report',
  '/analytics/reports/quarterly':   'Quarterly Report',
  '/analytics/reports/annual':      'Annual Report',
  '/settings':                      'Settings',
  '/settings/notifications':        'Notification Settings',
  '/settings/database':             'Database Settings',
  '/profile':                       'My Profile',
  '/notifications':                 'Notifications',
  '/audit':                         'Audit Log',
  '/reports':                       'Reports',
  '/system':                        'System Health',
  '/invite':                        'Invite User',
  '/developer-guide':               'Developer Guide',
  '/docs':                          'Dev Docs',
  '/ui-docs':                       'UI Component Library',
  '/login':                         'Sign in',
  '/forgot-password':               'Forgot Password',
  '/reset-password':                'Reset Password',
};

export function usePageTitle(override?: string) {
  const { pathname } = useLocation();
  useEffect(() => {
    const page = override ?? TITLE_MAP[pathname] ?? pathname.split('/').filter(Boolean).map((s) => s.charAt(0).toUpperCase() + s.slice(1)).join(' › ');
    document.title = page ? `${page} — ${env.VITE_APP_TITLE}` : env.VITE_APP_TITLE;
  }, [pathname, override]);
}
