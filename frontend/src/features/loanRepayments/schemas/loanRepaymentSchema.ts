import { z } from 'zod';

export const PAYMENT_METHOD_OPTIONS = ['bank_transfer', 'card', 'cash'] as const;

export const loanRepaymentSchema = z.object({
  loanAccountId: z.string().uuid(),
  loanInstallmentId: z.string().uuid(),
  amount: z.number().positive(),
  paymentMethod: z.enum(PAYMENT_METHOD_OPTIONS),
});
export type LoanRepaymentFormData = z.infer<typeof loanRepaymentSchema>;

export const completeLoanRepaymentSchema = z.object({
  referenceNumber: z.string().min(1).max(100),
});
export type CompleteLoanRepaymentFormData = z.infer<typeof completeLoanRepaymentSchema>;

export const failLoanRepaymentSchema = z.object({
  failureReason: z.string().min(1).max(1000),
});
export type FailLoanRepaymentFormData = z.infer<typeof failLoanRepaymentSchema>;
