import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import {
  LOAN_APPROVALS_BASE_PATH,
  LOAN_APPROVALS_KEY,
  type CreateLoanApprovalRequest,
  type ApproveLoanApprovalRequest,
  type RejectLoanApprovalRequest,
  type LoanApproval,
} from '../api/loanApprovalsApi';

function resource() {
  return useCrudResource<LoanApproval, CreateLoanApprovalRequest, never>(
    LOAN_APPROVALS_BASE_PATH,
    LOAN_APPROVALS_KEY,
  );
}

export function useLoanApprovals(params: ListParams = {}) {
  return resource().useList(params);
}
export function useLoanApproval(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateLoanApproval() {
  return resource().useCreate();
}
export function useDeleteLoanApproval() {
  return resource().useRemove();
}

export function useApproveLoanApproval() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: ApproveLoanApprovalRequest }) =>
      apiClient.post<ApiResponse<LoanApproval>>(`${LOAN_APPROVALS_BASE_PATH}/${id}/approve`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_APPROVALS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_APPROVALS_KEY, 'detail', id] });
      notify('Loan approved', 'success');
    },
  });
}

export function useRejectLoanApproval() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: RejectLoanApprovalRequest }) =>
      apiClient.post<ApiResponse<LoanApproval>>(`${LOAN_APPROVALS_BASE_PATH}/${id}/reject`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_APPROVALS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_APPROVALS_KEY, 'detail', id] });
      notify('Loan approval rejected', 'success');
    },
  });
}
