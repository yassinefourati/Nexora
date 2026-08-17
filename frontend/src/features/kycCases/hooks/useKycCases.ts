import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import { KYC_CASES_BASE_PATH, KYC_CASES_KEY, type CreateKycCaseRequest, type KycCase } from '../api/kycCasesApi';

function resource() {
  return useCrudResource<KycCase, CreateKycCaseRequest, never>(KYC_CASES_BASE_PATH, KYC_CASES_KEY);
}

export function useKycCases(params: ListParams = {}) {
  return resource().useList(params);
}
export function useKycCase(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateKycCase() {
  return resource().useCreate();
}

export function useStartKycReview() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) =>
      apiClient.post<ApiResponse<KycCase>>(`${KYC_CASES_BASE_PATH}/${id}/start-review`).then(unwrap),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: [KYC_CASES_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [KYC_CASES_KEY, 'detail', id] });
      notify('KYC review started', 'success');
    },
  });
}

export function useCompleteKycCase() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, outcome, reason }: { id: string; outcome: string; reason?: string }) =>
      apiClient.post<ApiResponse<KycCase>>(`${KYC_CASES_BASE_PATH}/${id}/complete`, { outcome, reason }).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [KYC_CASES_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [KYC_CASES_KEY, 'detail', id] });
      notify('KYC case completed', 'success');
    },
  });
}
