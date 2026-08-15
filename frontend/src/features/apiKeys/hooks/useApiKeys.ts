import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import { API_KEYS_BASE_PATH, API_KEYS_KEY, type ApiKey, type ApiKeyCreatedResponse, type CreateApiKeyRequest } from '../api/apiKeysApi';

function resource() {
  return useCrudResource<ApiKey, CreateApiKeyRequest, never>(API_KEYS_BASE_PATH, API_KEYS_KEY);
}

export function useApiKeysList(params: ListParams = {}) {
  return resource().useList(params);
}

/** Creation returns the plaintext secret exactly once — never retrievable again. */
export function useCreateApiKey() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (body: CreateApiKeyRequest) =>
      apiClient.post<ApiResponse<ApiKeyCreatedResponse>>(API_KEYS_BASE_PATH, body).then(unwrap),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: [API_KEYS_KEY, 'list'] });
      notify('API key created', 'success');
    },
  });
}

export function useRevokeApiKey() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) => apiClient.patch<ApiResponse<ApiKey>>(`${API_KEYS_BASE_PATH}/${id}/revoke`).then(unwrap),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: [API_KEYS_KEY] });
      notify('API key revoked', 'success');
    },
  });
}
