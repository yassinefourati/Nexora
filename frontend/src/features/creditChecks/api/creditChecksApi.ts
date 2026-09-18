import type { components } from '@/shared/types/api.generated';

export type CreditCheck = components['schemas']['CreditCheckResponse'];
export type CreateCreditCheckRequest = components['schemas']['CreateCreditCheckRequest'];

export const CREDIT_CHECKS_BASE_PATH = '/credit-checks';
export const CREDIT_CHECKS_KEY = 'creditChecks';
