import { z } from 'zod';

export const entityTagSchema = z.object({
  tagId: z.string().uuid('A tag is required'),
  entityType: z.string().min(1).max(100),
  entityId: z.string().uuid('A valid entity ID is required'),
});
export type EntityTagFormData = z.infer<typeof entityTagSchema>;
