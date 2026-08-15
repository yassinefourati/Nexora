import { z } from 'zod';
import { SETTING_SCOPES } from '../api/settingsApi';

export const createSettingSchema = z.object({
  scope: z.enum(SETTING_SCOPES),
  organizationId: z.string().uuid().optional().or(z.literal('')),
  key: z.string().min(1).max(150),
  value: z.string().optional(),
  description: z.string().max(500).optional(),
  editable: z.boolean(),
});
export type CreateSettingFormData = z.infer<typeof createSettingSchema>;

export const updateSettingSchema = z.object({
  value: z.string().optional(),
  description: z.string().max(500).optional(),
  editable: z.boolean(),
});
export type UpdateSettingFormData = z.infer<typeof updateSettingSchema>;
