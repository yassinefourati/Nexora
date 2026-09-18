import { z } from 'zod';

export const documentSchema = z.object({
  documentType: z.enum(['identity', 'proof_of_address', 'proof_of_income', 'employment_letter',
    'bank_statement', 'tax_document', 'credit_report', 'signed_contract', 'loan_offer', 'other']),
  category: z.enum(['identity', 'financial', 'legal', 'supporting']),
  fileName: z.string().min(1).max(255),
  storageKey: z.string().min(1).max(500),
  contentType: z.string().max(100).optional().or(z.literal('')),
  sizeBytes: z.number().int().min(0).optional(),
});
export type DocumentFormData = z.infer<typeof documentSchema>;
