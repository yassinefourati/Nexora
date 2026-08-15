import { z } from 'zod';

export const roleSchema = z.object({
  name: z.string().min(1).max(100),
  description: z.string().max(255).optional(),
  system: z.boolean().optional(),
});
export type RoleFormData = z.infer<typeof roleSchema>;
