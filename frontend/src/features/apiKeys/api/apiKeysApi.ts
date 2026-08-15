import type { components } from '@/shared/types/api.generated';

export type ApiKey = components['schemas']['ApiKeyResponse'];
export type CreateApiKeyRequest = components['schemas']['CreateApiKeyRequest'];
export type ApiKeyCreatedResponse = components['schemas']['ApiKeyCreatedResponse'];

export const API_KEYS_BASE_PATH = '/api-keys';
export const API_KEYS_KEY = 'api-keys';
