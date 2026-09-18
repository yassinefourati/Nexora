import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import { CREDIT_CHECKS_BASE_PATH, CREDIT_CHECKS_KEY, type CreateCreditCheckRequest, type CreditCheck } from '../api/creditChecksApi';

function resource() {
  return useCrudResource<CreditCheck, CreateCreditCheckRequest, never>(CREDIT_CHECKS_BASE_PATH, CREDIT_CHECKS_KEY);
}

export function useCreditChecks(params: ListParams = {}) {
  return resource().useList(params);
}
export function useCreditCheck(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateCreditCheck() {
  return resource().useCreate();
}

export function useProcessCreditCheck() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) =>
      apiClient.post<ApiResponse<CreditCheck>>(`${CREDIT_CHECKS_BASE_PATH}/${id}/process`).then(unwrap),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: [CREDIT_CHECKS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [CREDIT_CHECKS_KEY, 'detail', id] });
      notify('Credit check processed', 'success');
    },
  });
}
