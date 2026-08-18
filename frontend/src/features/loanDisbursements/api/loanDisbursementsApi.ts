import type { components } from '@/shared/types/api.generated';

export type LoanDisbursement = components['schemas']['LoanDisbursementResponse'];
export type CreateLoanDisbursementRequest = components['schemas']['CreateLoanDisbursementRequest'];
export type CompleteLoanDisbursementRequest = components['schemas']['CompleteLoanDisbursementRequest'];
export type FailLoanDisbursementRequest = components['schemas']['FailLoanDisbursementRequest'];

export const LOAN_DISBURSEMENTS_BASE_PATH = '/loan-disbursements';
export const LOAN_DISBURSEMENTS_KEY = 'loanDisbursements';
