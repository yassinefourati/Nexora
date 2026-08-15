import { z } from 'zod';

export const createNotificationTemplateSchema = z.object({
  code: z.string().min(1).max(100),
  name: z.string().min(1).max(150),
  subjectTemplate: z.string().max(255).optional(),
  bodyTemplate: z.string().min(1),
  channel: z.string().min(1).max(20),
});
export type CreateNotificationTemplateFormData = z.infer<typeof createNotificationTemplateSchema>;

export const updateNotificationTemplateSchema = z.object({
  name: z.string().min(1).max(150),
  subjectTemplate: z.string().max(255).optional(),
  bodyTemplate: z.string().min(1),
  channel: z.string().min(1).max(20),
});
export type UpdateNotificationTemplateFormData = z.infer<typeof updateNotificationTemplateSchema>;
