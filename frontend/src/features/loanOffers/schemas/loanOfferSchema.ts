import { z } from 'zod';

export const loanOfferSchema = z.object({
  loanApplicationId: z.string().uuid(),
  loanApprovalId: z.string().uuid(),
  expiresAt: z.string().min(1),
});
export type LoanOfferFormData = z.infer<typeof loanOfferSchema>;

export const declineLoanOfferSchema = z.object({
  declineReason: z.string().min(1).max(1000),
});
export type DeclineLoanOfferFormData = z.infer<typeof declineLoanOfferSchema>;
