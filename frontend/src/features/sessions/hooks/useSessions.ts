import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import { SESSIONS_BASE_PATH, SESSIONS_KEY, type Session } from '../api/sessionsApi';

function resource() {
  return useCrudResource<Session, never, never>(SESSIONS_BASE_PATH, SESSIONS_KEY);
}

export function useSessionsList(params: ListParams = {}) {
  return resource().useList(params);
}

export function useRevokeSession() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) => apiClient.patch<ApiResponse<Session>>(`${SESSIONS_BASE_PATH}/${id}/revoke`).then(unwrap),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: [SESSIONS_KEY] });
      notify('Session revoked', 'success');
    },
  });
}
