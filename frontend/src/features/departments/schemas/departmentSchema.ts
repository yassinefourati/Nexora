import { z } from 'zod';

export const createDepartmentSchema = z.object({
  name: z.string().min(1).max(200),
  code: z.string().max(50).optional(),
  organizationId: z.string().uuid('An organization is required'),
  parentDepartmentId: z.string().uuid().optional().or(z.literal('')),
});
export type CreateDepartmentFormData = z.infer<typeof createDepartmentSchema>;

export const updateDepartmentSchema = z.object({
  name: z.string().min(1).max(200),
  code: z.string().max(50).optional(),
  parentDepartmentId: z.string().uuid().optional().or(z.literal('')),
});
export type UpdateDepartmentFormData = z.infer<typeof updateDepartmentSchema>;
