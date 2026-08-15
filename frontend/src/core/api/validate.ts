import type { ZodType } from 'zod';

/**
 * Validates an API response against a Zod schema at runtime.
 * On failure: logs a warning (with field-level details) and returns the raw
 * data so the app keeps running — schema drift doesn't hard-crash users.
 *
 * Usage:
 *   import { z } from 'zod';
 *   import { validated } from '@/core/api/validate';
 *
 *   const UserSchema = z.object({ id: z.number(), name: z.string(), email: z.string() });
 *   export const getUser = (id: number) =>
 *     apiClient.get(`/users/${id}`).then((r) => validated(UserSchema)(r.data));
 */
export function validated<T>(schema: ZodType<T>) {
  return (data: unknown): T => {
    const result = schema.safeParse(data);
    if (!result.success) {
      console.warn(
        '[API] Response validation failed — schema drift detected:',
        result.error.flatten(),
      );
      return data as T;
    }
    return result.data;
  };
}

/** Wraps a promise-returning API call and validates the resolved value. */
export async function validatedAsync<T>(schema: ZodType<T>, call: Promise<unknown>): Promise<T> {
  const data = await call;
  return validated(schema)(data);
}
