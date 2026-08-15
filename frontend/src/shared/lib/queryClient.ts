import { QueryClient } from '@tanstack/react-query';

/** Centralised stale-time constants — import these in every useQuery call */
export const STALE = {
  REALTIME:  1_000 * 15,          // 15 s  — live dashboard, notifications
  SHORT:     1_000 * 60 * 2,      // 2 min — user lists, audit log
  MEDIUM:    1_000 * 60 * 5,      // 5 min — analytics, settings (default)
  LONG:      1_000 * 60 * 30,     // 30 min — roles/permissions matrix
  STATIC:    Infinity,             // never re-fetch — locale, feature flags
} as const;

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: STALE.MEDIUM,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});
