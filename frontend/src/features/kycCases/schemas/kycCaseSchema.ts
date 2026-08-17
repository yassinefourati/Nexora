import { z } from 'zod';

export const kycCaseSchema = z.object({
  customerId: z.string().uuid(),
});
export type KycCaseFormData = z.infer<typeof kycCaseSchema>;
