import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import {
  LOAN_APPLICATIONS_BASE_PATH,
  LOAN_APPLICATIONS_KEY,
  type CreateLoanApplicationRequest,
  type UpdateLoanApplicationRequest,
  type LoanApplication,
} from '../api/loanApplicationsApi';

function resource() {
  return useCrudResource<LoanApplication, CreateLoanApplicationRequest, UpdateLoanApplicationRequest>(
    LOAN_APPLICATIONS_BASE_PATH,
    LOAN_APPLICATIONS_KEY,
  );
}

export function useLoanApplications(params: ListParams = {}) {
  return resource().useList(params);
}
export function useLoanApplication(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateLoanApplication() {
  return resource().useCreate();
}
export function useUpdateLoanApplication() {
  return resource().useUpdate();
}
export function useDeleteLoanApplication() {
  return resource().useRemove();
}

export function useSubmitLoanApplication() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) =>
      apiClient.post<ApiResponse<LoanApplication>>(`${LOAN_APPLICATIONS_BASE_PATH}/${id}/submit`).then(unwrap),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_APPLICATIONS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_APPLICATIONS_KEY, 'detail', id] });
      notify('Loan application submitted', 'success');
    },
  });
}

export function useCancelLoanApplication() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason?: string }) =>
      apiClient.post<ApiResponse<LoanApplication>>(`${LOAN_APPLICATIONS_BASE_PATH}/${id}/cancel`, { reason }).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_APPLICATIONS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_APPLICATIONS_KEY, 'detail', id] });
      notify('Loan application cancelled', 'success');
    },
  });
}
