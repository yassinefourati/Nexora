import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { FEATURE_FLAGS_BASE_PATH, FEATURE_FLAGS_KEY, type CreateFeatureFlagRequest, type UpdateFeatureFlagRequest, type FeatureFlag } from '../api/featureFlagsApi';

function useResource() {
  return useCrudResource<FeatureFlag, CreateFeatureFlagRequest, UpdateFeatureFlagRequest>(FEATURE_FLAGS_BASE_PATH, FEATURE_FLAGS_KEY);
}

export function useFeatureFlags(params: ListParams = {}) {
  return useResource().useList(params);
}
export function useCreateFeatureFlag() {
  return useResource().useCreate();
}
export function useUpdateFeatureFlag() {
  return useResource().useUpdate();
}
export function useDeleteFeatureFlag() {
  return useResource().useRemove();
}
