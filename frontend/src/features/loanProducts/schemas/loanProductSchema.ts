import { z } from 'zod';

export const loanProductSchema = z.object({
  code: z.string().min(1).max(50),
  name: z.string().min(1).max(200),
  productType: z.enum(['personal', 'consumer', 'auto', 'mortgage', 'business', 'credit_line']),
  status: z.string().max(20).optional(),
  currency: z.string().max(3).optional(),
  minAmount: z.number().min(0),
  maxAmount: z.number().min(0),
  minTermMonths: z.number().int().min(1),
  maxTermMonths: z.number().int().min(1),
  description: z.string().optional().or(z.literal('')),
});
export type LoanProductFormData = z.infer<typeof loanProductSchema>;
