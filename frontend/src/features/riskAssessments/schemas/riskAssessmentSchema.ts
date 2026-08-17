import { z } from 'zod';

export const riskAssessmentSchema = z.object({
  loanApplicationId: z.string().uuid(),
});
export type RiskAssessmentFormData = z.infer<typeof riskAssessmentSchema>;
