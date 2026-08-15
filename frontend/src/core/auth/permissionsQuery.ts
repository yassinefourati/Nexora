import { useQuery } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useAuthStore } from '@/core/auth/stores/useAuthStore';
import { STALE } from '@/shared/lib/queryClient';
import type { Action, Resource } from '@/shared/types/roles';
import type { components } from '@/shared/types/api.generated';

type PermissionResponse = components['schemas']['PermissionResponse'];

export interface EffectivePermission { resource: Resource; action: Action; }

/**
 * Resolves the current user's effective permissions via the backend's
 * /me/permissions endpoint, which does the roles -> role-permissions ->
 * permissions join server-side in one query instead of the N+1 request
 * chain this used to do client-side.
 */
export function useEffectivePermissions() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);

  const permissionsQuery = useQuery({
    queryKey: ['auth', 'effective-permissions'],
    queryFn: () => apiClient.get<ApiResponse<PermissionResponse[]>>('/me/permissions').then(unwrap),
    select: (permissions): EffectivePermission[] =>
      permissions
        .filter((p) => p.resource && p.action)
        .map((p) => ({ resource: p.resource as Resource, action: p.action as Action })),
    enabled: isAuthenticated,
    staleTime: STALE.LONG,
  });

  return {
    data: permissionsQuery.data,
    isLoading: permissionsQuery.isLoading,
    isError: permissionsQuery.isError,
  };
}
