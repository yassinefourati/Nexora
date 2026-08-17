import type { components } from '@/shared/types/api.generated';

export type FraudCheck = components['schemas']['FraudCheckResponse'];
export type CreateFraudCheckRequest = components['schemas']['CreateFraudCheckRequest'];

export const FRAUD_CHECKS_BASE_PATH = '/fraud-checks';
export const FRAUD_CHECKS_KEY = 'fraudChecks';
