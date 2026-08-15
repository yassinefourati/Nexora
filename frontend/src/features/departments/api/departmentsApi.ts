import type { components } from '@/shared/types/api.generated';

export type Department = components['schemas']['DepartmentResponse'];
export type CreateDepartmentRequest = components['schemas']['CreateDepartmentRequest'];
export type UpdateDepartmentRequest = components['schemas']['UpdateDepartmentRequest'];

export const DEPARTMENTS_BASE_PATH = '/departments';
export const DEPARTMENTS_KEY = 'departments';
