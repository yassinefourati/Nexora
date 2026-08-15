import { z } from 'zod';

export const createFeatureFlagSchema = z.object({
  key: z.string().min(1).max(100),
  name: z.string().min(1).max(150),
  description: z.string().max(500).optional(),
  organizationId: z.string().uuid().optional().or(z.literal('')),
  enabled: z.boolean(),
});
export type CreateFeatureFlagFormData = z.infer<typeof createFeatureFlagSchema>;

export const updateFeatureFlagSchema = z.object({
  name: z.string().min(1).max(150),
  description: z.string().max(500).optional(),
  enabled: z.boolean(),
});
export type UpdateFeatureFlagFormData = z.infer<typeof updateFeatureFlagSchema>;
