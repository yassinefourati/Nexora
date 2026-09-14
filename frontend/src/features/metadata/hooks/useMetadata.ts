import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { METADATA_BASE_PATH, METADATA_KEY, type CreateMetadataKvRequest, type UpdateMetadataKvRequest, type MetadataKv } from '../api/metadataApi';

function useResource() {
  return useCrudResource<MetadataKv, CreateMetadataKvRequest, UpdateMetadataKvRequest>(METADATA_BASE_PATH, METADATA_KEY);
}

export function useMetadata(params: ListParams = {}) {
  return useResource().useList(params);
}
export function useCreateMetadata() {
  return useResource().useCreate();
}
export function useUpdateMetadata() {
  return useResource().useUpdate();
}
export function useDeleteMetadata() {
  return useResource().useRemove();
}
