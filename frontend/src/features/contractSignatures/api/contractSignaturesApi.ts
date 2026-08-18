import type { components } from '@/shared/types/api.generated';

export type ContractSignature = components['schemas']['ContractSignatureResponse'];
export type CreateContractSignatureRequest = components['schemas']['CreateContractSignatureRequest'];
export type DeclineContractSignatureRequest = components['schemas']['DeclineContractSignatureRequest'];

export const CONTRACT_SIGNATURES_BASE_PATH = '/contract-signatures';
export const CONTRACT_SIGNATURES_KEY = 'contractSignatures';
