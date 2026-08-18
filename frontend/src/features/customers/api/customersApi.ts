import type { components } from '@/shared/types/api.generated';

export type Customer = components['schemas']['CustomerResponse'];
export type CreateCustomerRequest = components['schemas']['CreateCustomerRequest'];
export type UpdateCustomerRequest = components['schemas']['UpdateCustomerRequest'];

export const CUSTOMERS_BASE_PATH = '/customers';
export const CUSTOMERS_KEY = 'customers';
