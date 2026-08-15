import { describe, it, expect } from 'vitest';
import type { EffectivePermission } from '@/core/auth/permissionsQuery';
import type { Action, Resource } from '@/shared/types/roles';

vi.mock('@/core/api/client', () => ({ default: { post: vi.fn(), interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } } }, setupInterceptors: vi.fn() }));

function can(granted: EffectivePermission[], resource: Resource, action: Action) {
  return granted.some((p) => p.resource === resource && p.action === action);
}

const adminGrants: EffectivePermission[] = [
  { resource: 'user', action: 'read' }, { resource: 'user', action: 'write' },
  { resource: 'user', action: 'edit' }, { resource: 'user', action: 'delete' },
  { resource: 'settings', action: 'edit' },
];
const managerGrants: EffectivePermission[] = [
  { resource: 'user', action: 'read' }, { resource: 'user', action: 'edit' },
];
const userGrants: EffectivePermission[] = [
  { resource: 'user', action: 'read' },
];

describe('effective permissions matching', () => {
  it('admin has full user access', () => {
    expect(can(adminGrants, 'user', 'delete')).toBe(true);
    expect(can(adminGrants, 'settings', 'edit')).toBe(true);
  });
  it('manager cannot delete users', () => {
    expect(can(managerGrants, 'user', 'delete')).toBe(false);
    expect(can(managerGrants, 'user', 'edit')).toBe(true);
  });
  it('user is read-only', () => {
    expect(can(userGrants, 'user', 'read')).toBe(true);
    expect(can(userGrants, 'user', 'write')).toBe(false);
    expect(can(userGrants, 'settings', 'edit')).toBe(false);
  });
});
