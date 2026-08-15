import type { components } from '@/shared/types/api.generated';

export type ErrorLog = components['schemas']['ErrorLogResponse'];

export const ERROR_LOGS_BASE_PATH = '/error-logs';
export const ERROR_LOGS_KEY = 'error-logs';
