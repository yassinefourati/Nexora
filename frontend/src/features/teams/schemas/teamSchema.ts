import { z } from 'zod';

export const createTeamSchema = z.object({
  name: z.string().min(1).max(200),
  organizationId: z.string().uuid('An organization is required'),
  departmentId: z.string().uuid().optional().or(z.literal('')),
});
export type CreateTeamFormData = z.infer<typeof createTeamSchema>;

export const updateTeamSchema = z.object({
  name: z.string().min(1).max(200),
  departmentId: z.string().uuid().optional().or(z.literal('')),
});
export type UpdateTeamFormData = z.infer<typeof updateTeamSchema>;
