import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import { DOCUMENTS_BASE_PATH, DOCUMENTS_KEY, type CreateDocumentRequest, type Document } from '../api/documentsApi';

function resource() {
  return useCrudResource<Document, CreateDocumentRequest, never>(DOCUMENTS_BASE_PATH, DOCUMENTS_KEY);
}

export function useDocuments(params: ListParams = {}) {
  return resource().useList(params);
}
export function useDocument(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateDocument() {
  return resource().useCreate();
}
export function useDeleteDocument() {
  return resource().useRemove();
}

export function useReviewDocument() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, decision, comments }: { id: string; decision: 'verified' | 'rejected'; comments?: string }) =>
      apiClient.put<ApiResponse<Document>>(`${DOCUMENTS_BASE_PATH}/${id}/review`, { decision, comments }).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [DOCUMENTS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [DOCUMENTS_KEY, 'detail', id] });
      notify('Document reviewed', 'success');
    },
  });
}
