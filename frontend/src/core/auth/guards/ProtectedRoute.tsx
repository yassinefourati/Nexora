import { Navigate } from 'react-router-dom';
import { LinearProgress } from '@mui/material';
import { useAuthStore } from '@/core/auth/stores/useAuthStore';
import { usePermission } from '@/shared/hooks/usePermission';
import { useEffectivePermissions } from '@/core/auth/permissionsQuery';
import { ROUTES } from '@/core/router/routes';
import type { Action, Resource } from '@/shared/types/roles';
import type { ReactNode } from 'react';

interface Props { children: ReactNode; resource?: Resource; action?: Action; }
export default function ProtectedRoute({ children, resource, action }: Props) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const isInitialized = useAuthStore((s) => s.isInitialized);
  const allowed = usePermission(resource ?? 'user', action ?? 'read');
  const { isLoading: permissionsLoading } = useEffectivePermissions();

  if (!isInitialized) return <LinearProgress sx={{ position: 'fixed', top: 0, left: 0, right: 0, zIndex: 9999 }} />;
  if (!isAuthenticated) return <Navigate to={ROUTES.LOGIN} replace />;
  // Do not bounce to /403 while the permission set is still loading: an authorised user
  // would otherwise be redirected on every hard refresh before /me/permissions answers.
  if (resource && permissionsLoading) return <LinearProgress sx={{ position: 'fixed', top: 0, left: 0, right: 0, zIndex: 9999 }} />;
  if (resource && !allowed) return <Navigate to='/403' replace />;
  return <>{children}</>;
}
