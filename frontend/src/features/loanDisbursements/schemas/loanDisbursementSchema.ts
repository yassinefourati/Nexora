import { z } from 'zod';

export const DISBURSEMENT_METHOD_OPTIONS = ['bank_transfer', 'check', 'wire'] as const;

export const loanDisbursementSchema = z.object({
  loanApplicationId: z.string().uuid(),
  loanContractId: z.string().uuid(),
  disbursementMethod: z.enum(DISBURSEMENT_METHOD_OPTIONS),
  destinationAccount: z.string().min(1).max(100),
});
export type LoanDisbursementFormData = z.infer<typeof loanDisbursementSchema>;

export const completeLoanDisbursementSchema = z.object({
  referenceNumber: z.string().min(1).max(100),
});
export type CompleteLoanDisbursementFormData = z.infer<typeof completeLoanDisbursementSchema>;

export const failLoanDisbursementSchema = z.object({
  failureReason: z.string().min(1).max(1000),
});
export type FailLoanDisbursementFormData = z.infer<typeof failLoanDisbursementSchema>;
