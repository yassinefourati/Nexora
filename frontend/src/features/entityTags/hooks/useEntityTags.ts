import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { ENTITY_TAGS_BASE_PATH, ENTITY_TAGS_KEY, type CreateEntityTagRequest, type EntityTag } from '../api/entityTagsApi';

function resource() {
  return useCrudResource<EntityTag, CreateEntityTagRequest, never>(ENTITY_TAGS_BASE_PATH, ENTITY_TAGS_KEY);
}

export function useEntityTags(params: ListParams = {}) {
  return resource().useList(params);
}
export function useCreateEntityTag() {
  return resource().useCreate();
}
export function useDeleteEntityTag() {
  return resource().useRemove();
}
