import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import {
  LOAN_CONTRACTS_BASE_PATH,
  LOAN_CONTRACTS_KEY,
  type CreateLoanContractRequest,
  type FinalizeLoanContractRequest,
  type CancelLoanContractRequest,
  type LoanContract,
} from '../api/loanContractsApi';

function resource() {
  return useCrudResource<LoanContract, CreateLoanContractRequest, never>(
    LOAN_CONTRACTS_BASE_PATH,
    LOAN_CONTRACTS_KEY,
  );
}

export function useLoanContracts(params: ListParams = {}) {
  return resource().useList(params);
}
export function useLoanContract(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateLoanContract() {
  return resource().useCreate();
}
export function useDeleteLoanContract() {
  return resource().useRemove();
}

export function useFinalizeLoanContract() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: FinalizeLoanContractRequest }) =>
      apiClient.post<ApiResponse<LoanContract>>(`${LOAN_CONTRACTS_BASE_PATH}/${id}/finalize`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_CONTRACTS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_CONTRACTS_KEY, 'detail', id] });
      notify('Loan contract finalized', 'success');
    },
  });
}

export function useCancelLoanContract() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: CancelLoanContractRequest }) =>
      apiClient.post<ApiResponse<LoanContract>>(`${LOAN_CONTRACTS_BASE_PATH}/${id}/cancel`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_CONTRACTS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_CONTRACTS_KEY, 'detail', id] });
      notify('Loan contract cancelled', 'success');
    },
  });
}
