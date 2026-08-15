import { describe, it, expect } from 'vitest';
import { createUserSchema, updateUserSchema } from '@/features/users/schemas/userSchema';

describe('createUserSchema', () => {
  const valid = { username: 'jdoe', email: 'j@example.com', password: 'password123', firstName: 'Jane', lastName: 'Doe' };

  it('accepts a valid payload matching CreateUserRequest', () => {
    expect(createUserSchema.safeParse(valid).success).toBe(true);
  });

  it('rejects a password shorter than the backend minimum of 8 characters', () => {
    const result = createUserSchema.safeParse({ ...valid, password: 'short' });
    expect(result.success).toBe(false);
  });

  it('rejects an invalid email', () => {
    const result = createUserSchema.safeParse({ ...valid, email: 'not-an-email' });
    expect(result.success).toBe(false);
  });

  it('requires username, email, password, firstName, and lastName', () => {
    const result = createUserSchema.safeParse({});
    expect(result.success).toBe(false);
    if (!result.success) {
      const paths = result.error.issues.map((i) => i.path[0]);
      expect(paths).toEqual(expect.arrayContaining(['username', 'email', 'password', 'firstName', 'lastName']));
    }
  });

  it('does not require status or superuser — both optional on CreateUserRequest', () => {
    expect(createUserSchema.safeParse(valid).success).toBe(true);
  });
});

describe('updateUserSchema', () => {
  const valid = { email: 'j@example.com', firstName: 'Jane', lastName: 'Doe', status: 'active' };

  it('accepts a valid payload matching UpdateUserRequest', () => {
    expect(updateUserSchema.safeParse(valid).success).toBe(true);
  });

  it('has no username or password fields — those are immutable via this endpoint', () => {
    const parsed = updateUserSchema.safeParse({ ...valid, username: 'ignored', password: 'ignored' });
    expect(parsed.success).toBe(true);
    if (parsed.success) {
      expect(parsed.data).not.toHaveProperty('username');
      expect(parsed.data).not.toHaveProperty('password');
    }
  });

  it('requires status, unlike CreateUserRequest where it is optional', () => {
    const { status: _status, ...withoutStatus } = valid;
    const result = updateUserSchema.safeParse(withoutStatus);
    expect(result.success).toBe(false);
  });
});
