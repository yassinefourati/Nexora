import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrapPage, unwrap, type ApiResponse } from '@/core/api/envelope';
import { STALE } from '@/shared/lib/queryClient';
import { useAppStore } from '@/shared/stores/useAppStore';
import { COMMENTS_BASE_PATH, COMMENTS_KEY, type Comment, type CreateCommentRequest, type UpdateCommentRequest } from '../api/commentsApi';

/** Comments are always scoped to a specific entity — there is no global comments list on the backend. */
export function useComments(entityType: string, entityId: string | undefined, params: { page?: number; size?: number } = {}) {
  return useQuery({
    queryKey: [COMMENTS_KEY, entityType, entityId, params],
    queryFn: () => apiClient.get<ApiResponse<Comment[]>>(COMMENTS_BASE_PATH, { params: { entityType, entityId, ...params } }).then(unwrapPage),
    enabled: Boolean(entityType && entityId),
    staleTime: STALE.SHORT,
  });
}

export function useCreateComment() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (body: CreateCommentRequest) => apiClient.post<ApiResponse<Comment>>(COMMENTS_BASE_PATH, body).then(unwrap),
    onSuccess: (_data, body) => {
      void queryClient.invalidateQueries({ queryKey: [COMMENTS_KEY, body.entityType, body.entityId] });
      notify('Comment added', 'success');
    },
  });
}

export function useUpdateComment(entityType: string, entityId: string) {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateCommentRequest }) =>
      apiClient.put<ApiResponse<Comment>>(`${COMMENTS_BASE_PATH}/${id}`, body).then(unwrap),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: [COMMENTS_KEY, entityType, entityId] });
      notify('Comment updated', 'success');
    },
  });
}

export function useDeleteComment(entityType: string, entityId: string) {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) => apiClient.delete<ApiResponse<void>>(`${COMMENTS_BASE_PATH}/${id}`).then(unwrap),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: [COMMENTS_KEY, entityType, entityId] });
      notify('Comment deleted', 'success');
    },
  });
}
