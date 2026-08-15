import type { components } from '@/shared/types/api.generated';

export type Session = components['schemas']['SessionResponse'];

export const SESSIONS_BASE_PATH = '/sessions';
export const SESSIONS_KEY = 'sessions';
