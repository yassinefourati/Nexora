import { z } from 'zod';

export const creditCheckSchema = z.object({
  loanApplicationId: z.string().uuid(),
  customerId: z.string().uuid(),
});
export type CreditCheckFormData = z.infer<typeof creditCheckSchema>;
