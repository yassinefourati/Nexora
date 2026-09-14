import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { TAGS_BASE_PATH, TAGS_KEY, type CreateTagRequest, type UpdateTagRequest, type Tag } from '../api/tagsApi';

function useResource() {
  return useCrudResource<Tag, CreateTagRequest, UpdateTagRequest>(TAGS_BASE_PATH, TAGS_KEY);
}

export function useTags(params: ListParams = {}) {
  return useResource().useList(params);
}
export function useCreateTag() {
  return useResource().useCreate();
}
export function useUpdateTag() {
  return useResource().useUpdate();
}
export function useDeleteTag() {
  return useResource().useRemove();
}
