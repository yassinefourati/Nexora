import { z } from 'zod';

/** Matches CreateUserRequest exactly — username/email/password/firstName/lastName required. */
export const createUserSchema = z.object({
  username: z.string().min(1).max(100),
  email: z.string().email().max(255),
  password: z.string().min(8).max(255),
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  status: z.string().max(20).optional(),
  superuser: z.boolean().optional(),
});
export type CreateUserFormData = z.infer<typeof createUserSchema>;

/** Matches UpdateUserRequest exactly — no username/password (immutable via this endpoint). */
export const updateUserSchema = z.object({
  email: z.string().email().max(255),
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  status: z.string().max(20),
  superuser: z.boolean().optional(),
});
export type UpdateUserFormData = z.infer<typeof updateUserSchema>;
