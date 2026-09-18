import { z } from 'zod';

export const underwritingCaseSchema = z.object({
  loanApplicationId: z.string().uuid(),
  assignedTo: z.string().max(150).optional().or(z.literal('')),
});
export type UnderwritingCaseFormData = z.infer<typeof underwritingCaseSchema>;

export const DECISION_OPTIONS = ['approve', 'approve_with_conditions', 'refer', 'reject', 'request_information'] as const;

export const decideUnderwritingCaseSchema = z.object({
  decision: z.enum(DECISION_OPTIONS),
  decisionReason: z.string().max(1000).optional().or(z.literal('')),
  approvedAmount: z.number().positive().optional(),
  approvedTermMonths: z.number().int().min(1).optional(),
});
export type DecideUnderwritingCaseFormData = z.infer<typeof decideUnderwritingCaseSchema>;
