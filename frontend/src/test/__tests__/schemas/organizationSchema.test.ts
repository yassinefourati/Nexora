import { describe, it, expect } from 'vitest';
import { organizationSchema } from '@/features/organizations/schemas/organizationSchema';

describe('organizationSchema', () => {
  it('accepts a valid payload matching CreateOrganizationRequest', () => {
    expect(organizationSchema.safeParse({ name: 'Acme Corp', code: 'ACME' }).success).toBe(true);
  });

  it('requires both name and code — the two required fields on CreateOrganizationRequest', () => {
    expect(organizationSchema.safeParse({ name: 'Acme Corp' }).success).toBe(false);
    expect(organizationSchema.safeParse({ code: 'ACME' }).success).toBe(false);
  });

  it('accepts an empty string for parentOrganizationId to mean "no parent"', () => {
    expect(organizationSchema.safeParse({ name: 'Acme Corp', code: 'ACME', parentOrganizationId: '' }).success).toBe(true);
  });

  it('rejects a non-UUID parentOrganizationId', () => {
    const result = organizationSchema.safeParse({ name: 'Acme Corp', code: 'ACME', parentOrganizationId: 'not-a-uuid' });
    expect(result.success).toBe(false);
  });

  it('accepts a real UUID for parentOrganizationId', () => {
    const result = organizationSchema.safeParse({ name: 'Acme Corp', code: 'ACME', parentOrganizationId: '55a60b5e-768e-4162-b2f6-6a842ee300e7' });
    expect(result.success).toBe(true);
  });
});
