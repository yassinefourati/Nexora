import { z } from 'zod';

export const loanApplicationSchema = z.object({
  customerId: z.string().uuid(),
  loanProductId: z.string().uuid(),
  requestedAmount: z.number().positive(),
  requestedTermMonths: z.number().int().min(1),
  purpose: z.string().max(200).optional().or(z.literal('')),
});
export type LoanApplicationFormData = z.infer<typeof loanApplicationSchema>;
