import type { components } from '@/shared/types/api.generated';

export type User = components['schemas']['UserResponse'];
export type CreateUserRequest = components['schemas']['CreateUserRequest'];
export type UpdateUserRequest = components['schemas']['UpdateUserRequest'];

export const USERS_BASE_PATH = '/users';
export const USERS_KEY = 'users';
