import { z } from 'zod';

export const createAppModuleSchema = z.object({
  key: z.string().min(1).max(100),
  name: z.string().min(1).max(150),
  description: z.string().max(500).optional(),
  active: z.boolean(),
});
export type CreateAppModuleFormData = z.infer<typeof createAppModuleSchema>;

export const updateAppModuleSchema = z.object({
  name: z.string().min(1).max(150),
  description: z.string().max(500).optional(),
  active: z.boolean(),
});
export type UpdateAppModuleFormData = z.infer<typeof updateAppModuleSchema>;
