import { z } from 'zod';

export const loanApprovalSchema = z.object({
  loanApplicationId: z.string().uuid(),
  underwritingCaseId: z.string().uuid(),
});
export type LoanApprovalFormData = z.infer<typeof loanApprovalSchema>;

export const approveLoanApprovalSchema = z.object({
  approvedAmount: z.number().positive(),
  approvedTermMonths: z.number().int().min(1),
  interestRate: z.number().min(0),
  approvedBy: z.string().min(1).max(150),
});
export type ApproveLoanApprovalFormData = z.infer<typeof approveLoanApprovalSchema>;

export const rejectLoanApprovalSchema = z.object({
  rejectionReason: z.string().min(1).max(1000),
});
export type RejectLoanApprovalFormData = z.infer<typeof rejectLoanApprovalSchema>;
