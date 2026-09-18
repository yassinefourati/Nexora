import { z } from 'zod';

export const STAGE_OPTIONS = ['reminder', 'notice', 'final_notice', 'agency'] as const;

export const collectionCaseSchema = z.object({
  loanAccountId: z.string().uuid(),
  loanInstallmentId: z.string().uuid(),
  assignedTo: z.string().max(150).optional().or(z.literal('')),
});
export type CollectionCaseFormData = z.infer<typeof collectionCaseSchema>;

export const escalateCollectionCaseSchema = z.object({
  stage: z.enum(STAGE_OPTIONS),
});
export type EscalateCollectionCaseFormData = z.infer<typeof escalateCollectionCaseSchema>;

export const resolveCollectionCaseSchema = z.object({
  resolutionNotes: z.string().min(1).max(1000),
});
export type ResolveCollectionCaseFormData = z.infer<typeof resolveCollectionCaseSchema>;

export const writeOffCollectionCaseSchema = z.object({
  resolutionNotes: z.string().min(1).max(1000),
});
export type WriteOffCollectionCaseFormData = z.infer<typeof writeOffCollectionCaseSchema>;
