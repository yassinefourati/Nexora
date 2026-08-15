import { z } from 'zod';

export const menuPermissionSchema = z.object({
  menuItemId: z.string().uuid('A menu item is required'),
  permissionId: z.string().uuid('A permission is required'),
});
export type MenuPermissionFormData = z.infer<typeof menuPermissionSchema>;
