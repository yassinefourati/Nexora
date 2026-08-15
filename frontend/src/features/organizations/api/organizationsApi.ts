import type { components } from '@/shared/types/api.generated';

export type Organization = components['schemas']['OrganizationResponse'];
export type CreateOrganizationRequest = components['schemas']['CreateOrganizationRequest'];
export type UpdateOrganizationRequest = components['schemas']['UpdateOrganizationRequest'];

export const ORGANIZATIONS_BASE_PATH = '/organizations';
export const ORGANIZATIONS_KEY = 'organizations';
