import { z } from 'zod';

export const roleMenuSchema = z.object({
  roleId: z.string().uuid('A role is required'),
  menuItemId: z.string().uuid('A menu item is required'),
  canView: z.boolean().optional(),
});
export type RoleMenuFormData = z.infer<typeof roleMenuSchema>;
