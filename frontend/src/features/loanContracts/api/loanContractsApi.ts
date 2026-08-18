import type { components } from '@/shared/types/api.generated';

export type LoanContract = components['schemas']['LoanContractResponse'];
export type CreateLoanContractRequest = components['schemas']['CreateLoanContractRequest'];
export type FinalizeLoanContractRequest = components['schemas']['FinalizeLoanContractRequest'];
export type CancelLoanContractRequest = components['schemas']['CancelLoanContractRequest'];

export const LOAN_CONTRACTS_BASE_PATH = '/loan-contracts';
export const LOAN_CONTRACTS_KEY = 'loanContracts';
