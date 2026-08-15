import { z } from 'zod';

export const createApiKeySchema = z.object({
  name: z.string().min(1).max(150),
  userId: z.string().uuid('A user is required'),
  scopes: z.array(z.string()).max(50).optional(),
  expiresAt: z.string().optional(),
});
export type CreateApiKeyFormData = z.infer<typeof createApiKeySchema>;
