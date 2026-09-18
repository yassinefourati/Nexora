import type { components } from '@/shared/types/api.generated';

export type LoanApproval = components['schemas']['LoanApprovalResponse'];
export type CreateLoanApprovalRequest = components['schemas']['CreateLoanApprovalRequest'];
export type ApproveLoanApprovalRequest = components['schemas']['ApproveLoanApprovalRequest'];
export type RejectLoanApprovalRequest = components['schemas']['RejectLoanApprovalRequest'];

export const LOAN_APPROVALS_BASE_PATH = '/loan-approvals';
export const LOAN_APPROVALS_KEY = 'loanApprovals';
