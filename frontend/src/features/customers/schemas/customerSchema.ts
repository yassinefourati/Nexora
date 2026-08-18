import { z } from 'zod';

export const customerSchema = z.object({
  customerType: z.enum(['individual', 'business']),
  status: z.string().max(20).optional(),
  firstName: z.string().max(100).optional().or(z.literal('')),
  lastName: z.string().max(100).optional().or(z.literal('')),
  businessName: z.string().max(200).optional().or(z.literal('')),
  dateOfBirth: z.string().optional().or(z.literal('')),
  nationalId: z.string().max(50).optional().or(z.literal('')),
  email: z.string().email().max(320),
  phone: z.string().max(30).optional().or(z.literal('')),
});
export type CustomerFormData = z.infer<typeof customerSchema>;
