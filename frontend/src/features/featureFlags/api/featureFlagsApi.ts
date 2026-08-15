import type { components } from '@/shared/types/api.generated';

export type FeatureFlag = components['schemas']['FeatureFlagResponse'];
export type CreateFeatureFlagRequest = components['schemas']['CreateFeatureFlagRequest'];
export type UpdateFeatureFlagRequest = components['schemas']['UpdateFeatureFlagRequest'];

export const FEATURE_FLAGS_BASE_PATH = '/feature-flags';
export const FEATURE_FLAGS_KEY = 'feature-flags';
