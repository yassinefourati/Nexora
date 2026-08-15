import type { ReactNode } from 'react';
import { usePermission } from '@/shared/hooks/usePermission';
import type { Action, Resource } from '@/shared/types/roles';

interface Props {
  resource: Resource;
  action: Action;
  children: ReactNode;
  fallback?: ReactNode;
}

/** Declarative RBAC gate — renders `children` only if the current user is allowed `action` on `resource`. */
export default function Can({ resource, action, children, fallback = null }: Props) {
  const allowed = usePermission(resource, action);
  return <>{allowed ? children : fallback}</>;
}
