import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import {
  COLLECTION_CASES_BASE_PATH,
  COLLECTION_CASES_KEY,
  type CreateCollectionCaseRequest,
  type EscalateCollectionCaseRequest,
  type ResolveCollectionCaseRequest,
  type WriteOffCollectionCaseRequest,
  type CollectionCase,
} from '../api/collectionCasesApi';

function resource() {
  return useCrudResource<CollectionCase, CreateCollectionCaseRequest, never>(
    COLLECTION_CASES_BASE_PATH,
    COLLECTION_CASES_KEY,
  );
}

export function useCollectionCases(params: ListParams = {}) {
  return resource().useList(params);
}
export function useCollectionCase(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateCollectionCase() {
  return resource().useCreate();
}
export function useDeleteCollectionCase() {
  return resource().useRemove();
}

export function useEscalateCollectionCase() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: EscalateCollectionCaseRequest }) =>
      apiClient.post<ApiResponse<CollectionCase>>(`${COLLECTION_CASES_BASE_PATH}/${id}/escalate`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [COLLECTION_CASES_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [COLLECTION_CASES_KEY, 'detail', id] });
      notify('Collection case escalated', 'success');
    },
  });
}

export function useResolveCollectionCase() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: ResolveCollectionCaseRequest }) =>
      apiClient.post<ApiResponse<CollectionCase>>(`${COLLECTION_CASES_BASE_PATH}/${id}/resolve`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [COLLECTION_CASES_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [COLLECTION_CASES_KEY, 'detail', id] });
      notify('Collection case resolved', 'success');
    },
  });
}

export function useWriteOffCollectionCase() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: WriteOffCollectionCaseRequest }) =>
      apiClient.post<ApiResponse<CollectionCase>>(`${COLLECTION_CASES_BASE_PATH}/${id}/write-off`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [COLLECTION_CASES_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [COLLECTION_CASES_KEY, 'detail', id] });
      notify('Collection case written off', 'success');
    },
  });
}
