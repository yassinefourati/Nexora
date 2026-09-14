import { z } from 'zod';

/** Matches CreateUserRequest. There is no password: sign-in is Keycloak's job (ADR 0001). */
export const createUserSchema = z.object({
  username: z.string().min(1).max(100),
  email: z.string().email().max(255),
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  status: z.string().max(20).optional(),
  superuser: z.boolean().optional(),
});
export type CreateUserFormData = z.infer<typeof createUserSchema>;

/** Matches UpdateUserRequest — no username; `version` carries the optimistic-lock token. */
export const updateUserSchema = z.object({
  email: z.string().email().max(255),
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  status: z.string().max(20),
  superuser: z.boolean().optional(),
  version: z.number().int().optional(),
});
export type UpdateUserFormData = z.infer<typeof updateUserSchema>;
