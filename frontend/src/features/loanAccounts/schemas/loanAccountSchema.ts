import { z } from 'zod';

export const loanAccountSchema = z.object({
  loanApplicationId: z.string().uuid(),
  loanDisbursementId: z.string().uuid(),
  accountNumber: z.string().min(1).max(50),
});
export type LoanAccountFormData = z.infer<typeof loanAccountSchema>;

export const closeLoanAccountSchema = z.object({
  reason: z.string().max(500).optional().or(z.literal('')),
});
export type CloseLoanAccountFormData = z.infer<typeof closeLoanAccountSchema>;

export const defaultLoanAccountSchema = z.object({
  reason: z.string().min(1).max(500),
});
export type DefaultLoanAccountFormData = z.infer<typeof defaultLoanAccountSchema>;
