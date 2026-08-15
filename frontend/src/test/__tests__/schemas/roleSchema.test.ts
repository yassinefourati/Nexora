import { describe, it, expect } from 'vitest';
import { roleSchema } from '@/features/roles/schemas/roleSchema';

describe('roleSchema', () => {
  it('accepts a valid payload matching CreateRoleRequest/UpdateRoleRequest', () => {
    expect(roleSchema.safeParse({ name: 'manager', description: 'Elevated access', system: false }).success).toBe(true);
  });

  it('requires a non-empty name — the only required field on CreateRoleRequest', () => {
    expect(roleSchema.safeParse({}).success).toBe(false);
    expect(roleSchema.safeParse({ name: '' }).success).toBe(false);
  });

  it('description and system are optional', () => {
    expect(roleSchema.safeParse({ name: 'auditor' }).success).toBe(true);
  });

  it('rejects a name longer than the backend max length of 100', () => {
    expect(roleSchema.safeParse({ name: 'a'.repeat(101) }).success).toBe(false);
  });
});
