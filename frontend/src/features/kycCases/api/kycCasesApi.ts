import type { components } from '@/shared/types/api.generated';

export type KycCase = components['schemas']['KycCaseResponse'];
export type CreateKycCaseRequest = components['schemas']['CreateKycCaseRequest'];

export const KYC_CASES_BASE_PATH = '/kyc-cases';
export const KYC_CASES_KEY = 'kycCases';
