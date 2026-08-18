import { z } from 'zod';

export const SIGNER_ROLE_OPTIONS = ['primary_applicant', 'co_applicant', 'guarantor'] as const;
export const SIGNATURE_METHOD_OPTIONS = ['electronic', 'wet_ink'] as const;

export const contractSignatureSchema = z.object({
  loanContractId: z.string().uuid(),
  signerName: z.string().min(1).max(200),
  signerRole: z.enum(SIGNER_ROLE_OPTIONS),
  signatureMethod: z.enum(SIGNATURE_METHOD_OPTIONS).optional(),
});
export type ContractSignatureFormData = z.infer<typeof contractSignatureSchema>;

export const declineContractSignatureSchema = z.object({
  declineReason: z.string().min(1).max(1000),
});
export type DeclineContractSignatureFormData = z.infer<typeof declineContractSignatureSchema>;
