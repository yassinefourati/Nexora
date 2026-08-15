import { useEffectivePermissions } from '@/core/auth/permissionsQuery';
import type { Action, Resource } from '@/shared/types/roles';

export function usePermission(resource: Resource, action: Action): boolean {
  const { data: effective } = useEffectivePermissions();
  return effective?.some((p) => p.resource === resource && p.action === action) ?? false;
}
