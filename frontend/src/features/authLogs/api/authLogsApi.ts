import type { components } from '@/shared/types/api.generated';

export type AuthLog = components['schemas']['AuthLogResponse'];

export const AUTH_LOGS_BASE_PATH = '/auth-logs';
export const AUTH_LOGS_KEY = 'auth-logs';
