import type { components } from '@/shared/types/api.generated';

export type MetadataKv = components['schemas']['MetadataKvResponse'];
export type CreateMetadataKvRequest = components['schemas']['CreateMetadataKvRequest'];
export type UpdateMetadataKvRequest = components['schemas']['UpdateMetadataKvRequest'];

export const METADATA_BASE_PATH = '/metadata';
export const METADATA_KEY = 'metadata';
