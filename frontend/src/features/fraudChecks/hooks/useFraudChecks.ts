import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import { FRAUD_CHECKS_BASE_PATH, FRAUD_CHECKS_KEY, type CreateFraudCheckRequest, type FraudCheck } from '../api/fraudChecksApi';

function resource() {
  return useCrudResource<FraudCheck, CreateFraudCheckRequest, never>(FRAUD_CHECKS_BASE_PATH, FRAUD_CHECKS_KEY);
}

export function useFraudChecks(params: ListParams = {}) {
  return resource().useList(params);
}
export function useFraudCheck(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateFraudCheck() {
  return resource().useCreate();
}

export function useProcessFraudCheck() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) =>
      apiClient.post<ApiResponse<FraudCheck>>(`${FRAUD_CHECKS_BASE_PATH}/${id}/process`).then(unwrap),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: [FRAUD_CHECKS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [FRAUD_CHECKS_KEY, 'detail', id] });
      notify('Fraud check processed', 'success');
    },
  });
}
