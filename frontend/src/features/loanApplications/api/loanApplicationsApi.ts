import type { components } from '@/shared/types/api.generated';

export type LoanApplication = components['schemas']['LoanApplicationResponse'];
export type CreateLoanApplicationRequest = components['schemas']['CreateLoanApplicationRequest'];
export type UpdateLoanApplicationRequest = components['schemas']['UpdateLoanApplicationRequest'];

export const LOAN_APPLICATIONS_BASE_PATH = '/loan-applications';
export const LOAN_APPLICATIONS_KEY = 'loanApplications';
