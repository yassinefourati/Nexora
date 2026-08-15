import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { STALE } from '@/shared/lib/queryClient';
import { useAppStore } from '@/shared/stores/useAppStore';
import { ATTACHMENTS_BASE_PATH, ATTACHMENTS_KEY, type Attachment, type CreateAttachmentRequest } from '../api/attachmentsApi';

/**
 * Attachments are always scoped to a specific entity, and the list endpoint
 * returns the full set (no pageable param) — the backend only stores
 * file metadata + a URL, not the binary itself.
 */
export function useAttachments(entityType: string, entityId: string | undefined) {
  return useQuery({
    queryKey: [ATTACHMENTS_KEY, entityType, entityId],
    queryFn: () => apiClient.get<ApiResponse<Attachment[]>>(ATTACHMENTS_BASE_PATH, { params: { entityType, entityId } }).then(unwrap),
    enabled: Boolean(entityType && entityId),
    staleTime: STALE.SHORT,
  });
}

export function useCreateAttachment() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (body: CreateAttachmentRequest) => apiClient.post<ApiResponse<Attachment>>(ATTACHMENTS_BASE_PATH, body).then(unwrap),
    onSuccess: (_data, body) => {
      void queryClient.invalidateQueries({ queryKey: [ATTACHMENTS_KEY, body.entityType, body.entityId] });
      notify('Attachment added', 'success');
    },
  });
}

export function useDeleteAttachment(entityType: string, entityId: string) {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) => apiClient.delete<ApiResponse<void>>(`${ATTACHMENTS_BASE_PATH}/${id}`).then(unwrap),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: [ATTACHMENTS_KEY, entityType, entityId] });
      notify('Attachment removed', 'success');
    },
  });
}
