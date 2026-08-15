import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { FEATURE_FLAGS_BASE_PATH, FEATURE_FLAGS_KEY, type CreateFeatureFlagRequest, type UpdateFeatureFlagRequest, type FeatureFlag } from '../api/featureFlagsApi';

function resource() {
  return useCrudResource<FeatureFlag, CreateFeatureFlagRequest, UpdateFeatureFlagRequest>(FEATURE_FLAGS_BASE_PATH, FEATURE_FLAGS_KEY);
}

export function useFeatureFlags(params: ListParams = {}) {
  return resource().useList(params);
}
export function useCreateFeatureFlag() {
  return resource().useCreate();
}
export function useUpdateFeatureFlag() {
  return resource().useUpdate();
}
export function useDeleteFeatureFlag() {
  return resource().useRemove();
}
