import { z } from 'zod';
export const settingsSchema = z.object({ name: z.string().min(2, 'Name must be at least 2 characters'), email: z.string().email('Invalid email'), language: z.enum(['en', 'fr']) });
export type SettingsFormData = z.infer<typeof settingsSchema>;
