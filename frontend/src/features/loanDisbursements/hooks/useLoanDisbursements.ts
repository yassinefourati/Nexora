import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import {
  LOAN_DISBURSEMENTS_BASE_PATH,
  LOAN_DISBURSEMENTS_KEY,
  type CreateLoanDisbursementRequest,
  type CompleteLoanDisbursementRequest,
  type FailLoanDisbursementRequest,
  type LoanDisbursement,
} from '../api/loanDisbursementsApi';

function resource() {
  return useCrudResource<LoanDisbursement, CreateLoanDisbursementRequest, never>(
    LOAN_DISBURSEMENTS_BASE_PATH,
    LOAN_DISBURSEMENTS_KEY,
  );
}

export function useLoanDisbursements(params: ListParams = {}) {
  return resource().useList(params);
}
export function useLoanDisbursement(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateLoanDisbursement() {
  return resource().useCreate();
}
export function useDeleteLoanDisbursement() {
  return resource().useRemove();
}

export function useInitiateLoanDisbursement() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) =>
      apiClient.post<ApiResponse<LoanDisbursement>>(`${LOAN_DISBURSEMENTS_BASE_PATH}/${id}/initiate`).then(unwrap),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_DISBURSEMENTS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_DISBURSEMENTS_KEY, 'detail', id] });
      notify('Loan disbursement initiated', 'success');
    },
  });
}

export function useCompleteLoanDisbursement() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: CompleteLoanDisbursementRequest }) =>
      apiClient.post<ApiResponse<LoanDisbursement>>(`${LOAN_DISBURSEMENTS_BASE_PATH}/${id}/complete`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_DISBURSEMENTS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_DISBURSEMENTS_KEY, 'detail', id] });
      notify('Loan disbursement completed', 'success');
    },
  });
}

export function useFailLoanDisbursement() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: FailLoanDisbursementRequest }) =>
      apiClient.post<ApiResponse<LoanDisbursement>>(`${LOAN_DISBURSEMENTS_BASE_PATH}/${id}/fail`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_DISBURSEMENTS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_DISBURSEMENTS_KEY, 'detail', id] });
      notify('Loan disbursement marked as failed', 'success');
    },
  });
}
