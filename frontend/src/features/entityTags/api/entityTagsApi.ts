import type { components } from '@/shared/types/api.generated';

export type EntityTag = components['schemas']['EntityTagResponse'];
export type CreateEntityTagRequest = components['schemas']['CreateEntityTagRequest'];

export const ENTITY_TAGS_BASE_PATH = '/entity-tags';
export const ENTITY_TAGS_KEY = 'entity-tags';
