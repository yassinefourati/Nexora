import { z } from 'zod';

export const createMenuSchema = z.object({
  name: z.string().min(1).max(150),
  code: z.string().min(1).max(100),
  description: z.string().optional(),
  active: z.boolean().optional(),
});
export type CreateMenuFormData = z.infer<typeof createMenuSchema>;

export const updateMenuSchema = z.object({
  name: z.string().min(1).max(150),
  description: z.string().optional(),
  active: z.boolean().optional(),
});
export type UpdateMenuFormData = z.infer<typeof updateMenuSchema>;

export const createMenuItemSchema = z.object({
  menuId: z.string().uuid('A menu is required'),
  parentMenuItemId: z.string().uuid().optional().or(z.literal('')),
  label: z.string().min(1).max(150),
  routePath: z.string().max(255).optional(),
  moduleKey: z.string().max(100).optional(),
  icon: z.string().max(100).optional(),
  sortOrder: z.number().int().optional(),
  active: z.boolean().optional(),
});
export type CreateMenuItemFormData = z.infer<typeof createMenuItemSchema>;

export const updateMenuItemSchema = z.object({
  parentMenuItemId: z.string().uuid().optional().or(z.literal('')),
  label: z.string().min(1).max(150),
  routePath: z.string().max(255).optional(),
  moduleKey: z.string().max(100).optional(),
  icon: z.string().max(100).optional(),
  sortOrder: z.number().int().optional(),
  active: z.boolean().optional(),
});
export type UpdateMenuItemFormData = z.infer<typeof updateMenuItemSchema>;
