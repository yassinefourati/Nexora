import type { components } from '@/shared/types/api.generated';

export type Tag = components['schemas']['TagResponse'];
export type CreateTagRequest = components['schemas']['CreateTagRequest'];
export type UpdateTagRequest = components['schemas']['UpdateTagRequest'];

export const TAGS_BASE_PATH = '/tags';
export const TAGS_KEY = 'tags';
