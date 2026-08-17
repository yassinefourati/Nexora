import { z } from 'zod';

export const fraudCheckSchema = z.object({
  loanApplicationId: z.string().uuid(),
});
export type FraudCheckFormData = z.infer<typeof fraudCheckSchema>;
