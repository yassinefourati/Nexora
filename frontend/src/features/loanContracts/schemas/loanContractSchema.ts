import { z } from 'zod';

export const loanContractSchema = z.object({
  loanApplicationId: z.string().uuid(),
  loanOfferId: z.string().uuid(),
  contractNumber: z.string().min(1).max(50),
});
export type LoanContractFormData = z.infer<typeof loanContractSchema>;

export const finalizeLoanContractSchema = z.object({
  documentUrl: z.string().max(500).optional().or(z.literal('')),
});
export type FinalizeLoanContractFormData = z.infer<typeof finalizeLoanContractSchema>;

export const cancelLoanContractSchema = z.object({
  cancellationReason: z.string().min(1).max(1000),
});
export type CancelLoanContractFormData = z.infer<typeof cancelLoanContractSchema>;
