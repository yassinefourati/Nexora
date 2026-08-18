import type { components } from '@/shared/types/api.generated';

export type LoanAccount = components['schemas']['LoanAccountResponse'];
export type LoanInstallment = components['schemas']['LoanInstallmentResponse'];
export type CreateLoanAccountRequest = components['schemas']['CreateLoanAccountRequest'];
export type CloseLoanAccountRequest = components['schemas']['CloseLoanAccountRequest'];
export type DefaultLoanAccountRequest = components['schemas']['DefaultLoanAccountRequest'];

export const LOAN_ACCOUNTS_BASE_PATH = '/loan-accounts';
export const LOAN_ACCOUNTS_KEY = 'loanAccounts';
export const LOAN_INSTALLMENTS_BASE_PATH = '/loan-installments';
