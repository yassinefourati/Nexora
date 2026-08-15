import { z } from 'zod';
import { PERMISSION_ACTIONS } from '../api/permissionsApi';

export const permissionSchema = z.object({
  resource: z.string().min(1).max(100),
  action: z.enum(PERMISSION_ACTIONS),
  description: z.string().max(255).optional(),
});
export type PermissionFormData = z.infer<typeof permissionSchema>;
