import type { components } from '@/shared/types/api.generated';

export type LoanProduct = components['schemas']['LoanProductResponse'];
export type CreateLoanProductRequest = components['schemas']['CreateLoanProductRequest'];
export type UpdateLoanProductRequest = components['schemas']['UpdateLoanProductRequest'];

export const LOAN_PRODUCTS_BASE_PATH = '/loan-products';
export const LOAN_PRODUCTS_KEY = 'loanProducts';
