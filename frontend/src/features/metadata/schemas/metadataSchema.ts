import { z } from 'zod';

export const createMetadataSchema = z.object({
  entityType: z.string().min(1).max(100),
  entityId: z.string().uuid('A valid entity ID is required'),
  key: z.string().min(1).max(150),
  value: z.string().min(1),
});
export type CreateMetadataFormData = z.infer<typeof createMetadataSchema>;

export const updateMetadataSchema = z.object({
  value: z.string().min(1),
});
export type UpdateMetadataFormData = z.infer<typeof updateMetadataSchema>;
