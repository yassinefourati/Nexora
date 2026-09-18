import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import {
  LOAN_REPAYMENTS_BASE_PATH,
  LOAN_REPAYMENTS_KEY,
  type CreateLoanRepaymentRequest,
  type CompleteLoanRepaymentRequest,
  type FailLoanRepaymentRequest,
  type LoanRepayment,
} from '../api/loanRepaymentsApi';

function resource() {
  return useCrudResource<LoanRepayment, CreateLoanRepaymentRequest, never>(
    LOAN_REPAYMENTS_BASE_PATH,
    LOAN_REPAYMENTS_KEY,
  );
}

export function useLoanRepayments(params: ListParams = {}) {
  return resource().useList(params);
}
export function useLoanRepayment(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateLoanRepayment() {
  return resource().useCreate();
}

export function useCompleteLoanRepayment() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: CompleteLoanRepaymentRequest }) =>
      apiClient.post<ApiResponse<LoanRepayment>>(`${LOAN_REPAYMENTS_BASE_PATH}/${id}/complete`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_REPAYMENTS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_REPAYMENTS_KEY, 'detail', id] });
      void queryClient.invalidateQueries({ queryKey: ['loanInstallments'] });
      void queryClient.invalidateQueries({ queryKey: ['loanAccounts'] });
      notify('Loan repayment completed', 'success');
    },
  });
}

export function useFailLoanRepayment() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: FailLoanRepaymentRequest }) =>
      apiClient.post<ApiResponse<LoanRepayment>>(`${LOAN_REPAYMENTS_BASE_PATH}/${id}/fail`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_REPAYMENTS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_REPAYMENTS_KEY, 'detail', id] });
      notify('Loan repayment marked as failed', 'success');
    },
  });
}
