import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { AxiosResponse } from 'axios';
import apiClient from '@/core/api/client';
import { unwrap, unwrapPage, type ApiPagination, type ApiResponse, type PageParams } from '@/core/api/envelope';
import { STALE } from '@/shared/lib/queryClient';
import { useAppStore } from '@/shared/stores/useAppStore';

export interface ListParams extends PageParams {
  [key: string]: string | number | undefined;
}

export interface ResourcePage<T> {
  items: T[];
  pagination: ApiPagination;
}

/**
 * Generic server-driven CRUD hook for a single backend resource collection.
 * One instance per admin module (Users, Roles, Organizations, ...) — all
 * pagination, filtering, and sorting is delegated to the real backend via
 * query params, matching its actual Pageable/ApiResponse contract exactly.
 * No client-side slicing: `useList` re-fetches whenever `params` changes.
 */
export function useCrudResource<TResponse, TCreate = Partial<TResponse>, TUpdate = Partial<TResponse>>(
  basePath: string,
  resourceKey: string,
) {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();

  function useList(params: ListParams = {}) {
    return useQuery({
      queryKey: [resourceKey, 'list', params],
      queryFn: () =>
        apiClient
          .get<ApiResponse<TResponse[]>>(basePath, { params })
          .then((res: AxiosResponse<ApiResponse<TResponse[]>>): ResourcePage<TResponse> => unwrapPage(res)),
      staleTime: STALE.SHORT,
      placeholderData: (prev) => prev,
    });
  }

  function useOne(id: string | undefined) {
    return useQuery({
      queryKey: [resourceKey, 'detail', id],
      queryFn: () => apiClient.get<ApiResponse<TResponse>>(`${basePath}/${id}`).then(unwrap),
      enabled: Boolean(id),
      staleTime: STALE.SHORT,
    });
  }

  function useCreate() {
    return useMutation({
      mutationFn: (body: TCreate) => apiClient.post<ApiResponse<TResponse>>(basePath, body).then(unwrap),
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: [resourceKey, 'list'] });
        notify('Created successfully', 'success');
      },
    });
  }

  function useUpdate() {
    return useMutation({
      mutationFn: ({ id, body }: { id: string; body: TUpdate }) =>
        apiClient.put<ApiResponse<TResponse>>(`${basePath}/${id}`, body).then(unwrap),
      onSuccess: (_data, { id }) => {
        void queryClient.invalidateQueries({ queryKey: [resourceKey, 'list'] });
        void queryClient.invalidateQueries({ queryKey: [resourceKey, 'detail', id] });
        notify('Updated successfully', 'success');
      },
    });
  }

  function useRemove() {
    return useMutation({
      mutationFn: (id: string) => apiClient.delete<ApiResponse<void>>(`${basePath}/${id}`).then(unwrap),
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: [resourceKey, 'list'] });
        notify('Deleted successfully', 'success');
      },
    });
  }

  return { useList, useOne, useCreate, useUpdate, useRemove };
}
