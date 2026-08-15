import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { ROUTES, GUIDE_ROUTE } from '@/core/router/routes';

export function useKeyboardShortcuts(onOpenSearch: () => void, onOpenHelp: () => void) {
  const navigate  = useNavigate();
  const lastKey   = useRef<string | null>(null);
  const lastTime  = useRef<number>(0);

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      const tag = (e.target as HTMLElement).tagName;
      if (['INPUT','TEXTAREA','SELECT'].includes(tag)) return;

      // Cmd/Ctrl+K → global search
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        onOpenSearch();
        return;
      }

      // ? → help modal
      if (e.key === '?' && !e.ctrlKey && !e.metaKey) {
        onOpenHelp();
        return;
      }

      // Two-key sequences: G then X
      const now = Date.now();
      if (lastKey.current === 'g' && now - lastTime.current < 1000) {
        const map: Record<string, string> = {
          d: ROUTES.HOME, u: ROUTES.USERS,
          s: ROUTES.SETTINGS, p: ROUTES.PROFILE, n: ROUTES.NOTIFICATIONS,
          l: ROUTES.AUDIT, i: GUIDE_ROUTE,
        };
        if (map[e.key]) { navigate(map[e.key]); lastKey.current = null; return; }
      }

      lastKey.current = e.key;
      lastTime.current = now;
    };

    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [navigate, onOpenSearch, onOpenHelp]);
}
