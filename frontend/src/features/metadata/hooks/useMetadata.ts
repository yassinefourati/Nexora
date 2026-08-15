import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { METADATA_BASE_PATH, METADATA_KEY, type CreateMetadataKvRequest, type UpdateMetadataKvRequest, type MetadataKv } from '../api/metadataApi';

function resource() {
  return useCrudResource<MetadataKv, CreateMetadataKvRequest, UpdateMetadataKvRequest>(METADATA_BASE_PATH, METADATA_KEY);
}

export function useMetadata(params: ListParams = {}) {
  return resource().useList(params);
}
export function useCreateMetadata() {
  return resource().useCreate();
}
export function useUpdateMetadata() {
  return resource().useUpdate();
}
export function useDeleteMetadata() {
  return resource().useRemove();
}
