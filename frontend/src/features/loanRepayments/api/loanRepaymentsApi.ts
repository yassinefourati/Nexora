import type { components } from '@/shared/types/api.generated';

export type LoanRepayment = components['schemas']['LoanRepaymentResponse'];
export type CreateLoanRepaymentRequest = components['schemas']['CreateLoanRepaymentRequest'];
export type CompleteLoanRepaymentRequest = components['schemas']['CompleteLoanRepaymentRequest'];
export type FailLoanRepaymentRequest = components['schemas']['FailLoanRepaymentRequest'];

export const LOAN_REPAYMENTS_BASE_PATH = '/loan-repayments';
export const LOAN_REPAYMENTS_KEY = 'loanRepayments';
