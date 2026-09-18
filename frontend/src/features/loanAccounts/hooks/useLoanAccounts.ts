import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import { STALE } from '@/shared/lib/queryClient';
import {
  LOAN_ACCOUNTS_BASE_PATH,
  LOAN_ACCOUNTS_KEY,
  LOAN_INSTALLMENTS_BASE_PATH,
  type CreateLoanAccountRequest,
  type CloseLoanAccountRequest,
  type DefaultLoanAccountRequest,
  type LoanAccount,
  type LoanInstallment,
} from '../api/loanAccountsApi';

function resource() {
  return useCrudResource<LoanAccount, CreateLoanAccountRequest, never>(
    LOAN_ACCOUNTS_BASE_PATH,
    LOAN_ACCOUNTS_KEY,
  );
}

export function useLoanAccounts(params: ListParams = {}) {
  return resource().useList(params);
}
export function useLoanAccount(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateLoanAccount() {
  return resource().useCreate();
}
export function useDeleteLoanAccount() {
  return resource().useRemove();
}

export function useLoanInstallments(loanAccountId: string | undefined) {
  return useQuery({
    queryKey: ['loanInstallments', loanAccountId],
    queryFn: () =>
      apiClient
        .get<ApiResponse<LoanInstallment[]>>(LOAN_INSTALLMENTS_BASE_PATH, { params: { loanAccountId } })
        .then(unwrap),
    enabled: Boolean(loanAccountId),
    staleTime: STALE.SHORT,
  });
}

export function useCloseLoanAccount() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: CloseLoanAccountRequest }) =>
      apiClient.post<ApiResponse<LoanAccount>>(`${LOAN_ACCOUNTS_BASE_PATH}/${id}/close`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_ACCOUNTS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_ACCOUNTS_KEY, 'detail', id] });
      notify('Loan account closed', 'success');
    },
  });
}

export function useDefaultLoanAccount() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: DefaultLoanAccountRequest }) =>
      apiClient.post<ApiResponse<LoanAccount>>(`${LOAN_ACCOUNTS_BASE_PATH}/${id}/default`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_ACCOUNTS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_ACCOUNTS_KEY, 'detail', id] });
      notify('Loan account marked as defaulted', 'success');
    },
  });
}
