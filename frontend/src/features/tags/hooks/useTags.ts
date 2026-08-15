import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { TAGS_BASE_PATH, TAGS_KEY, type CreateTagRequest, type UpdateTagRequest, type Tag } from '../api/tagsApi';

function resource() {
  return useCrudResource<Tag, CreateTagRequest, UpdateTagRequest>(TAGS_BASE_PATH, TAGS_KEY);
}

export function useTags(params: ListParams = {}) {
  return resource().useList(params);
}
export function useCreateTag() {
  return resource().useCreate();
}
export function useUpdateTag() {
  return resource().useUpdate();
}
export function useDeleteTag() {
  return resource().useRemove();
}
