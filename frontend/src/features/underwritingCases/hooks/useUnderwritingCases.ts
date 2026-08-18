import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import {
  UNDERWRITING_CASES_BASE_PATH,
  UNDERWRITING_CASES_KEY,
  type CreateUnderwritingCaseRequest,
  type DecideUnderwritingCaseRequest,
  type UnderwritingCase,
} from '../api/underwritingCasesApi';

function resource() {
  return useCrudResource<UnderwritingCase, CreateUnderwritingCaseRequest, never>(
    UNDERWRITING_CASES_BASE_PATH,
    UNDERWRITING_CASES_KEY,
  );
}

export function useUnderwritingCases(params: ListParams = {}) {
  return resource().useList(params);
}
export function useUnderwritingCase(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateUnderwritingCase() {
  return resource().useCreate();
}
export function useDeleteUnderwritingCase() {
  return resource().useRemove();
}

export function useStartReviewUnderwritingCase() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) =>
      apiClient.post<ApiResponse<UnderwritingCase>>(`${UNDERWRITING_CASES_BASE_PATH}/${id}/start-review`).then(unwrap),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: [UNDERWRITING_CASES_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [UNDERWRITING_CASES_KEY, 'detail', id] });
      notify('Underwriting review started', 'success');
    },
  });
}

export function useDecideUnderwritingCase() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: DecideUnderwritingCaseRequest }) =>
      apiClient.post<ApiResponse<UnderwritingCase>>(`${UNDERWRITING_CASES_BASE_PATH}/${id}/decide`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [UNDERWRITING_CASES_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [UNDERWRITING_CASES_KEY, 'detail', id] });
      notify('Underwriting decision recorded', 'success');
    },
  });
}
