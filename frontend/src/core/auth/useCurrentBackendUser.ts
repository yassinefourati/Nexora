import { useQuery } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useAuthStore } from '@/core/auth/stores/useAuthStore';
import { STALE } from '@/shared/lib/queryClient';
import type { components } from '@/shared/types/api.generated';

type UserResponse = components['schemas']['UserResponse'];

/**
 * Resolves the current session's backend User row via GET /me.
 *
 * The Keycloak JWT `sub` and the backend `users.id` are NOT the same UUID —
 * confirmed live (Keycloak subject `7f82130e-...` vs backend user id
 * `fdd794fe-...` for the same `superadmin` account). They're independently
 * provisioned and only linked by username. The backend resolves this the
 * same way, via the JWT's `preferred_username` claim. Anything that needs
 * "the current user's backend id" (profile, own sessions, own notifications)
 * must go through this hook — passing the JWT `sub` directly to a `userId`
 * param silently queries a user that doesn't exist.
 */
export function useCurrentBackendUser() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);

  return useQuery({
    queryKey: ['auth', 'current-backend-user'],
    queryFn: (): Promise<UserResponse> =>
      apiClient.get<ApiResponse<UserResponse>>('/me').then(unwrap),
    enabled: isAuthenticated,
    staleTime: STALE.LONG,
  });
}
