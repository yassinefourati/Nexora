import { z } from 'zod';

export const organizationSchema = z.object({
  name: z.string().min(1).max(200),
  code: z.string().min(1).max(50),
  status: z.string().max(20).optional(),
  parentOrganizationId: z.string().uuid().optional().or(z.literal('')),
});
export type OrganizationFormData = z.infer<typeof organizationSchema>;
