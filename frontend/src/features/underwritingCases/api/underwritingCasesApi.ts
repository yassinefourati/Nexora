import type { components } from '@/shared/types/api.generated';

export type UnderwritingCase = components['schemas']['UnderwritingCaseResponse'];
export type CreateUnderwritingCaseRequest = components['schemas']['CreateUnderwritingCaseRequest'];
export type DecideUnderwritingCaseRequest = components['schemas']['DecideUnderwritingCaseRequest'];

export const UNDERWRITING_CASES_BASE_PATH = '/underwriting-cases';
export const UNDERWRITING_CASES_KEY = 'underwritingCases';
